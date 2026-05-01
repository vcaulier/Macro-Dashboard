import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { CotService } from '../../core/services/cot.service';
import { CotNetData, ASSETS, Asset } from '../../core/models/cot.model';

@Component({
  selector: 'app-cot-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './cot-chart.component.html',
  styleUrl: './cot-chart.component.scss'
})
export class CotChartComponent implements OnInit {

  private cotService = inject(CotService);

  assets = ASSETS;
  selectedAsset = signal<Asset>('EUR');
  cotData = signal<CotNetData[]>([]);
  assetData = signal<CotNetData>({});

  chartData = computed(() => ({
    labels: this.assetData().map((d : CotNetData) => d.date),
    datasets: [
      {
        label: 'Hedgers (Commercial)',
        data: this.assetData().map((d : CotNetData) => d.hedgersNet),
        borderColor: '#e8a04d', fill: 'origin',
        backgroundColor: 'rgba(139,69,19,0.15)',
        pointRadius: 0, tension: 0.3
      },
      {
        label: 'Institutions (Large Spec.)',
        data: this.assetData().map((d : CotNetData) => d.institutionnalNet),
        borderColor: '#4f8ef7', fill: false,
        pointRadius: 0, tension: 0.3
      },
      {
        label: 'Retail (Non-Reportable)',
        data: this.assetData().map((d : CotNetData) => d.retailNet),
        borderColor: '#a47fd4', fill: false,
        pointRadius: 0, tension: 0.3
      }
    ]
  }));

  chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: { color: '#8892a4', usePointStyle: true }
      }
    },
    scales: {
      x: {
        grid: { color: 'rgba(255,255,255,0.05)' },
        ticks: { color: '#8892a4', maxTicksLimit: 8, maxRotation: 0 },
        border: { dash: [4, 4] }
      },
      y: {
        grid: { color: 'rgba(255,255,255,0.05)' },
        ticks: {
          color: '#8892a4',
          callback: (v: any) => v >= 1000 ? v / 1000 + 'k' : v
        },
        border: { dash: [4, 4] }
      }
    }
  };

  selectAsset(asset: Asset) {
    this.selectedAsset.set(asset);
    this.assetData.set(this.cotData()
      .filter((d : CotNetData) => d.asset === asset)
      .sort((a : CotNetData, b : CotNetData) => b.date.compareTo(a.date)));
  }

  private load() {
    this.cotService.getCotData().subscribe(data => {
      this.cotData.set(data);
    });
  }

  ngOnInit() { this.load(); }

}
