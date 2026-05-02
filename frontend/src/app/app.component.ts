import { Component } from '@angular/core';
import { CotChartComponent } from './features/cot-chart/cot-chart.component';
import { InterestRatesComponent } from './features/interest-rates/interest-rates.component';

@Component({
  selector: 'app-root',
  imports: [CotChartComponent, InterestRatesComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'macro-dashboard';
}
