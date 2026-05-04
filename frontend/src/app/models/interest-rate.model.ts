import { Asset } from "./asset.model";

export interface InterestRate {
  date: Date;
  asset: Asset;
  interestRate: number;
}
