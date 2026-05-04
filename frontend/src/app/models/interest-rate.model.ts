import { Asset } from "./asset.model";

export interface InterestRate {
  date: string;  
  asset: Asset;  
  interestRate: number;
}
