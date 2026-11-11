export type DiscountType = "Percentage" | "FixedAmount" | "FreeItem";

export interface Promocode {
  id: string;
  code: string;
  start_date: string;
  end_date: string;
  discount_type: DiscountType;
  discount_value: number | null;
  free_item_description: string | null;
  minimum_order_amount: number;
  description: string | null;
  image_urls: string[];
  upvotes: number;
  downvotes: number;
  vote_score: number;
  verified: boolean;
  created_at: string;
  service_id: string;
  service_name: string;
  service_logo_url: string | null;
  service_site_url: string;
  author_id: string;
  author_name: string | null;
  author_avatar_url: string | null;
}

export interface Post {
  id: string;
  title: string;
  content: string;
  image_urls: string[];
  tags: string[];
  upvotes: number;
  downvotes: number;
  vote_score: number;
  created_at: string;
  author_id: string;
  author_name: string | null;
  author_avatar_url: string | null;
}

export interface Service {
  id: string;
  name: string;
  site_url: string;
  logo_url: string | null;
  promocode_count: number;
}

export interface Banner {
  id: string;
  image_url: string;
  cta_title: Record<string, string>;
  cta_description: Record<string, string>;
  cta_url: string | null;
}

export type VoteState = "NONE" | "UPVOTE" | "DOWNVOTE";
export type ItemType = "PROMOCODE" | "POST";
