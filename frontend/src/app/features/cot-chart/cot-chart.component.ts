import { Component, OnInit, AfterViewInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { CotService } from '../../core/services/cot.service';
import { Asset, ASSETS } from '../../models/asset.model';
import { CotNetData } from '../../models/cot.model';

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
  assetData = signal<CotNetData[]>([]);

  chartData = computed(() => ({
    labels: this.assetData().map(d => d.date),
    datasets: [
      {
        label: 'Hedgers (Commercial)',
        data: this.assetData().map(d => d.hedgersNet),
        borderColor: '#e8a04d',
        fill: 'origin',
        backgroundColor: 'rgba(139,69,19,0.15)',
        pointRadius: 0,
        tension: 0.3
      },
      {
        label: 'Institutions (Large Spec.)',
        data: this.assetData().map(d => d.institutionnalNet),
        borderColor: '#4f8ef7',
        fill: false,
        pointRadius: 0,
        tension: 0.3
      },
      {
        label: 'Retail (Non-Reportable)',
        data: this.assetData().map(d => d.retailNet),
        borderColor: '#a47fd4',
        fill: false,
        pointRadius: 0,
        tension: 0.3
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
          callback: (v: any) => Math.abs(v) >= 1000 ? (v / 1000).toFixed(0) + 'k' : v
        },
        border: { dash: [4, 4] }
      }
    }
  };

  selectAsset(asset: Asset) {
    this.selectedAsset.set(asset);
    // Lecture synchrone depuis le cache du service
    this.assetData.set(this.cotService.getByAsset(asset));
  }

  ngAfterViewInit() {
    this.cotService.loadAll().subscribe(() => {
      this.assetData.set(this.cotService.getByAsset(this.selectedAsset()));
    });
  }
}