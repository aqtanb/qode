import { onSchedule } from 'firebase-functions/v2/scheduler';
import * as logger from 'firebase-functions/logger';
import * as admin from 'firebase-admin';

/**
 * Scheduled Cloud Function: Clean up old files from Firebase Storage
 * Schedule: Runs daily at 2:00 AM UTC
 * Purpose: Delete files older than 30 days to manage storage costs
 */
export const cleanupOldStorageFiles = onSchedule(
  {
    schedule: 'every day 02:00',
    timeZone: 'UTC',
    memory: '256MiB',
  },
  async (event) => {
    logger.info('Starting storage cleanup task');

    const bucket = admin.storage().bucket();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

    let deletedCount = 0;
    let errorCount = 0;
    let totalFiles = 0;

    try {
      // Get all files from the bucket
      const [files] = await bucket.getFiles();
      totalFiles = files.length;

      logger.info(`Found ${totalFiles} files in storage bucket`);

      // Process files in batches to avoid memory issues
      const batchSize = 100;
      for (let i = 0; i < files.length; i += batchSize) {
        const batch = files.slice(i, i + batchSize);

        await Promise.all(
          batch.map(async (file) => {
            try {
              const [metadata] = await file.getMetadata();

              // Check if metadata has time information
              if (!metadata.timeCreated) {
                logger.warn(`File ${file.name} has no timeCreated metadata, skipping`);
                return;
              }

              const fileCreatedAt = new Date(metadata.timeCreated);
              const fileUpdatedAt = metadata.updated ? new Date(metadata.updated) : fileCreatedAt;

              // Use the more recent date (updated or created)
              const fileDate = fileUpdatedAt > fileCreatedAt ? fileUpdatedAt : fileCreatedAt;

              // Check if file is older than 30 days
              if (fileDate < thirtyDaysAgo) {
                logger.info(`Deleting old file: ${file.name}`, {
                  fileName: file.name,
                  createdAt: fileCreatedAt.toISOString(),
                  updatedAt: fileUpdatedAt.toISOString(),
                  age: Math.floor((Date.now() - fileDate.getTime()) / (1000 * 60 * 60 * 24)),
                });

                await file.delete();
                deletedCount++;
              }
            } catch (error) {
              logger.error(`Error processing file ${file.name}:`, error);
              errorCount++;
            }
          })
        );

        logger.info(`Processed batch ${Math.floor(i / batchSize) + 1} of ${Math.ceil(files.length / batchSize)}`);
      }

      logger.info('Storage cleanup completed', {
        totalFiles,
        deletedCount,
        errorCount,
        retainedFiles: totalFiles - deletedCount,
      });
    } catch (error) {
      logger.error('Error during storage cleanup:', error);
      throw new Error('Storage cleanup failed');
    }
  }
);
