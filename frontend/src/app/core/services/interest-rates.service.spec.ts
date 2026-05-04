import { TestBed } from '@angular/core/testing';

import { InterestRatesService } from './interest-rates.service';

describe('InterestRatesService', () => {
  let service: InterestRatesService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InterestRatesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
