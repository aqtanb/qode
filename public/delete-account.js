// Firebase will be initialized automatically from /__/firebase/init.js
// This is loaded in the HTML and configured via Firebase Hosting
const auth = firebase.auth();
const functions = firebase.functions();

// State
let currentUser = null;
let lastError = null;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
  // Check if user is already signed in
  auth.onAuthStateChanged((user) => {
    if (user) {
      currentUser = user;
      // If on sign-in step, automatically move to confirmation
      if (document.getElementById('step-signin').classList.contains('hidden') === false) {
        showConfirmationStep();
      }
    }
  });

  // Show intro step by default
  showStep('intro');
});

// Step Navigation
function showStep(stepName) {
  const steps = ['intro', 'signin', 'confirm', 'processing', 'success', 'error'];
  steps.forEach(step => {
    const element = document.getElementById(`step-${step}`);
    if (element) {
      element.classList.add('hidden');
    }
  });

  const errorDisplay = document.getElementById('error-display');
  if (errorDisplay) {
    errorDisplay.classList.add('hidden');
  }

  const targetElement = document.getElementById(`step-${stepName}`);
  if (targetElement) {
    targetElement.classList.remove('hidden');
  } else if (stepName === 'error') {
    errorDisplay.classList.remove('hidden');
  }
}

// Go to Sign In Step
function goToSignIn() {
  showStep('signin');
}

// Show Confirmation Step
function showConfirmationStep() {
  if (!currentUser) {
    showError('Please sign in first.');
    showStep('signin');
    return;
  }

  document.getElementById('user-email').textContent = currentUser.email;
  document.getElementById('confirm-checkbox').checked = false;
  document.getElementById('delete-btn').disabled = true;
  showStep('confirm');
}

// Google Sign-In
async function signInWithGoogle() {
  const provider = new firebase.auth.GoogleAuthProvider();
  const errorElement = document.getElementById('signin-error');

  try {
    errorElement.classList.add('hidden');
    errorElement.textContent = '';

    const result = await auth.signInWithPopup(provider);
    currentUser = result.user;

    // Move to confirmation step
    showConfirmationStep();

  } catch (error) {
    console.error('Sign-in error:', error);

    let errorMessage = 'Failed to sign in. Please try again.';

    if (error.code === 'auth/popup-blocked') {
      errorMessage = 'Popup was blocked. Please allow popups for this site and try again.';
    } else if (error.code === 'auth/popup-closed-by-user') {
      errorMessage = 'Sign-in was cancelled. Please try again.';
    } else if (error.code === 'auth/network-request-failed') {
      errorMessage = 'Network error. Please check your connection and try again.';
    } else if (error.code === 'auth/cancelled-popup-request') {
      // User clicked sign-in multiple times, ignore
      return;
    }

    errorElement.textContent = errorMessage;
    errorElement.classList.remove('hidden');
  }
}

// Sign Out
async function signOut() {
  try {
    await auth.signOut();
    currentUser = null;
    showStep('intro');
  } catch (error) {
    console.error('Sign-out error:', error);
    showError('Failed to sign out. Please refresh the page.');
  }
}

// Toggle Delete Button based on checkbox
function toggleDeleteButton() {
  const checkbox = document.getElementById('confirm-checkbox');
  const deleteBtn = document.getElementById('delete-btn');
  deleteBtn.disabled = !checkbox.checked;
}

// Delete Account
async function deleteAccount() {
  if (!currentUser) {
    showError('You must be signed in to delete your account.');
    showStep('signin');
    return;
  }

  const checkbox = document.getElementById('confirm-checkbox');
  if (!checkbox.checked) {
    return;
  }

  // Show processing step
  showStep('processing');

  try {
    // Call the Cloud Function
    const deleteUserAccount = functions.httpsCallable('deleteUserAccount');
    const result = await deleteUserAccount();

    console.log('Deletion result:', result);

    // Sign out the user
    await auth.signOut();
    currentUser = null;

    // Show success with deletion details
    showSuccessStep(result.data);

  } catch (error) {
    console.error('Deletion error:', error);

    let errorMessage = 'An error occurred while deleting your account. Please try again.';
    let shouldRetry = true;

    if (error.code === 'unauthenticated') {
      errorMessage = 'Your session has expired. Please sign in again.';
      shouldRetry = false;
      await auth.signOut();
      currentUser = null;
      showStep('signin');
      return;
    } else if (error.code === 'internal') {
      errorMessage = 'An error occurred on the server. ';

      // Check if there's partial deletion data
      if (error.details && error.details.partialDeletion) {
        errorMessage += 'Some data may have been deleted. Please contact support at support@qodein.com for assistance.';
      } else {
        errorMessage += 'Please try again or contact support at support@qodein.com.';
      }
    } else if (error.message && error.message.includes('network')) {
      errorMessage = 'Network error. Please check your connection and try again.';
    }

    lastError = errorMessage;
    showErrorStep(errorMessage, shouldRetry);
  }
}

// Show Success Step
function showSuccessStep(data) {
  const resultsContainer = document.getElementById('deletion-results');
  resultsContainer.innerHTML = '';

  if (data && data.deletedItems) {
    const items = data.deletedItems;

    if (items.posts > 0) {
      const li = document.createElement('li');
      li.textContent = `${items.posts} post${items.posts !== 1 ? 's' : ''}`;
      resultsContainer.appendChild(li);
    }

    if (items.promocodes > 0) {
      const li = document.createElement('li');
      li.textContent = `${items.promocodes} promocode${items.promocodes !== 1 ? 's' : ''}`;
      resultsContainer.appendChild(li);
    }

    if (items.interactions > 0) {
      const li = document.createElement('li');
      li.textContent = `${items.interactions} interaction${items.interactions !== 1 ? 's' : ''}`;
      resultsContainer.appendChild(li);
    }

    if (items.reports > 0) {
      const li = document.createElement('li');
      li.textContent = `${items.reports} report${items.reports !== 1 ? 's' : ''}`;
      resultsContainer.appendChild(li);
    }

    if (items.blocks > 0) {
      const li = document.createElement('li');
      li.textContent = `${items.blocks} blocked user${items.blocks !== 1 ? 's' : ''}`;
      resultsContainer.appendChild(li);
    }

    if (items.userDoc) {
      const li = document.createElement('li');
      li.textContent = 'Your user profile';
      resultsContainer.appendChild(li);
    }

    if (items.authAccount) {
      const li = document.createElement('li');
      li.textContent = 'Your authentication account';
      resultsContainer.appendChild(li);
    }
  }

  // If no items were deleted, show a message
  if (resultsContainer.children.length === 0) {
    const li = document.createElement('li');
    li.textContent = 'Your authentication account (no other data was found)';
    resultsContainer.appendChild(li);
  }

  showStep('success');
}

// Show Error Step
function showErrorStep(message, canRetry = true) {
  const errorMessage = document.getElementById('error-message');
  errorMessage.textContent = message;
  showStep('error');
}

// Retry from Error
function retryFromError() {
  if (currentUser) {
    showConfirmationStep();
  } else {
    showStep('signin');
  }
}

// Navigate to Play Store
function goToPlayStore() {
  window.location.href = 'https://play.google.com/store/apps/details?id=com.qodein.qode';
}

// Helper to show error messages
function showError(message) {
  console.error(message);
}