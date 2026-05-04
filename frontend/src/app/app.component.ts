import { Component } from '@angular/core';
import { DisplayChartComponent } from './features/display-chart/display-chart.component';
import { InterestRatesComponent } from './features/interest-rates/interest-rates.component';

@Component({
  selector: 'app-root',
  imports: [DisplayChartComponent, InterestRatesComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'macro-dashboard';
}
