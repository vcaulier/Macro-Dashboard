import { TestBed } from '@angular/core/testing';

import { CotService } from './cot.service';

describe('CotService', () => {
  let service: CotService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CotService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
