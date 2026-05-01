import { Component, AfterViewInit, inject, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, Chart, registerables } from 'chart.js';
import { CotService } from '../../core/services/cot.service';
import { Asset, ASSETS } from '../../models/asset.model';
import { CotNetData } from '../../models/cot.model';

Chart.register(...registerables);

@Component({
  selector: 'app-cot-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './cot-chart.component.html',
  styleUrl: './cot-chart.component.scss'
})
export class CotChartComponent implements AfterViewInit {

  private cotService = inject(CotService);

  assets = ASSETS;
  selectedAsset = signal<Asset>('EUR');

  chartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      { label: 'Hedgers (Commercial)', data: [], borderColor: '#e8a04d',
        backgroundColor: 'rgba(139,69,19,0.15)', fill: 'origin', pointRadius: 0, tension: 0.3 },
      { label: 'Institutions (Large Spec.)', data: [], borderColor: '#4f8ef7',
        fill: false, pointRadius: 0, tension: 0.3 },
      { label: 'Retail (Non-Reportable)', data: [], borderColor: '#a47fd4',
        fill: false, pointRadius: 0, tension: 0.3 }
    ]
  };

  chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 0 },
    plugins: {
      legend: {
        position: 'bottom',
        labels: { color: '#8892a4', usePointStyle: true }
      }
    },
    scales: {
      x: {
        type: 'category',
        grid: { color: 'rgba(255,255,255,0.05)' },
        ticks: { color: '#8892a4', maxTicksLimit: 8, maxRotation: 0 },
        border: { dash: [4, 4] }
      },
      y: {
        type: 'linear',
        grid: { color: 'rgba(255,255,255,0.05)' },
        ticks: {
          color: '#8892a4',
          callback: (v: any) => Math.abs(v) >= 1000 ? (v / 1000).toFixed(0) + 'k' : v
        },
        border: { dash: [4, 4] }
      }
    }
  };

  @ViewChild(BaseChartDirective) chartDirective?: BaseChartDirective;

  private updateChartData(data: CotNetData[]) {
    this.chartData.labels = data.map(d => d.date);
    this.chartData.datasets[0].data = data.map(d => d.hedgersNet);
    this.chartData.datasets[1].data = data.map(d => d.institutionnalNet);
    this.chartData.datasets[2].data = data.map(d => d.retailNet);
    this.chartDirective?.update('none');
  }

  selectAsset(asset: Asset) {
    this.selectedAsset.set(asset);
    const data = this.cotService.getByAsset(asset);
    if (data.length > 0) this.updateChartData(data);
  }

  ngAfterViewInit() {
    this.cotService.loadAll().subscribe(() => {
      const data = this.cotService.getByAsset(this.selectedAsset());
      this.updateChartData(data);
    });
  }
}