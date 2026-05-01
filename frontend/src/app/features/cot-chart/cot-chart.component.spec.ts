import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CotChartComponent } from './cot-chart.component';

describe('CotChartComponent', () => {
  let component: CotChartComponent;
  let fixture: ComponentFixture<CotChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CotChartComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CotChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
