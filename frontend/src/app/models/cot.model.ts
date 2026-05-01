import { Asset } from "./asset.model";

export interface CotNetData {
  date: string;
  asset: Asset;
  hedgersLong: number;
  hedgersShort: number;
  hedgersNet: number;
  institutionnalLong: number;
  institutionnalShort: number;
  institutionnalNet: number;
  retailLong: number;
  retailShort: number;
  retailNet: number;
  openInterest: number;
}
