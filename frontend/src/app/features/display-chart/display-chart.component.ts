import { Component, AfterViewInit, inject, signal, ViewChild, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, Chart, registerables } from 'chart.js';
import { CotService } from '../../core/services/cot.service';
import { Asset, ASSETS } from '../../models/asset.model';
import { CotNetData } from '../../models/cot.model';
import { InterestRatesService } from '../../core/services/interest-rates.service';
import { InterestRate } from '../../models/interest-rate.model';

const crosshairPlugin = {
  id: 'crosshair',
  afterDraw(chart: any) {
    if (!chart._active?.length) return;
    const ctx = chart.ctx;
    const x = chart._active[0].element.x;
    const top = chart.chartArea.top;
    const bottom = chart.chartArea.bottom;
    ctx.save();
    ctx.beginPath();
    ctx.moveTo(x, top);
    ctx.lineTo(x, bottom);
    ctx.lineWidth = 1;
    ctx.strokeStyle = 'rgba(255,255,255,0.2)';
    ctx.setLineDash([4, 4]);
    ctx.stroke();
    ctx.restore();
  }
};

const zeroLinePlugin = {
  id: 'zeroLine',
  afterDraw(chart: any) {
    const ctx = chart.ctx;
    const yScale = chart.scales['y'];
    if (!yScale) return;
    const y = yScale.getPixelForValue(0);
    if (y < chart.chartArea.top || y > chart.chartArea.bottom) return;
    ctx.save();
    ctx.beginPath();
    ctx.moveTo(chart.chartArea.left, y);
    ctx.lineTo(chart.chartArea.right, y);
    ctx.lineWidth = 1.5;
    ctx.strokeStyle = 'rgba(255,255,255,0.25)';
    ctx.setLineDash([6, 4]);
    ctx.stroke();
    ctx.restore();
  }
};

Chart.register(...registerables, crosshairPlugin, zeroLinePlugin);

@Component({
  selector: 'app-display-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  changeDetection: ChangeDetectionStrategy.Default,
  templateUrl: './display-chart.component.html',
  styleUrl: './display-chart.component.scss'
})
export class DisplayChartComponent implements AfterViewInit {

  private readonly RATE_COLORS: Partial<Record<Asset, string>> = {
    AUD: '#4f8ef7',
    GBP: '#e8a04d',
    USD: '#a47fd4',
    EUR: '#4bc08a',
    JPY: '#f74f4f',
    CAD: '#f7c94f',
    NZD: '#4fd4d4',
    CHF: '#d4d4d4'
  };

  private cotService = inject(CotService);
  private cdr = inject(ChangeDetectorRef);

  private ratesService = inject(InterestRatesService);
  isRatesView = signal(false);

  assets = ASSETS;
  selectedAsset = signal<Asset | null>(null);
  
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
    events: ['mousemove', 'mouseout', 'click', 'touchstart', 'touchmove', 'mouseover'],
    interaction: {
      mode: 'index',
      intersect: false
    },
    plugins: {
      legend: {
        position: 'bottom',
        labels: { color: '#8892a4', usePointStyle: true }
      },
      tooltip: {
        backgroundColor: 'rgba(25,28,36,0.95)' as string,
        titleColor: '#e2e8f0' as string,
        bodyColor: '#8892a4' as string,
        borderColor: 'rgba(255,255,255,0.08)' as string,
        borderWidth: 1,
        padding: 10,
        callbacks: {
          label: (ctx: any) => {
            const y: number = ctx.parsed?.y ?? 0;
            if (this.isRatesView()) {
              return ` ${ctx.dataset.label}: ${y.toFixed(2)}%`;
            }
            const formatted = Math.abs(y) >= 1000
              ? (y / 1000).toFixed(1) + 'k'
              : y.toString();
            return ` ${ctx.dataset.label}: ${formatted}`;
          }
        }
      } as any
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
          callback: (v: any) => {
            if (this.isRatesView()) {
              return `${v}%`;
            }
            return Math.abs(v) >= 1000 ? (v / 1000).toFixed(0) + 'k' : v;
          }
        },
        border: { dash: [4, 4] }
      }
    }
  };

  @ViewChild(BaseChartDirective) chartDirective?: BaseChartDirective;

  private buildDataset(label: string, data: number[], color: string, fill: any) {
    const radii = data.map((_, i) => i === data.length - 1 ? 4 : 0);
    const pointColors = data.map((_, i) => i === data.length - 1 ? color : 'transparent');
    return {
      label,
      data,
      borderColor: color,
      backgroundColor: fill === 'origin' ? color.replace(')', ',0.15)').replace('rgb', 'rgba') : undefined,
      fill,
      borderDash: label.includes('Hedgers') ? [5, 4] : undefined,
      pointRadius: radii,
      pointBackgroundColor: pointColors,
      tension: 0.3
    };
  }

  private updateChartData(data: CotNetData[]) {
    this.chartData = {
      labels: data.map(d => d.date),
      datasets: [
        this.buildDataset('Hedgers (Commercial)', data.map(d => d.hedgersNet), '#e8a04d', false),
        this.buildDataset('Institutions (Large Spec.)', data.map(d => d.institutionnalNet), '#4f8ef7', false),
        this.buildDataset('Retail (Non-Reportable)', data.map(d => d.retailNet), '#a47fd4', false)
      ]
    };
    this.cdr.markForCheck();
    this.chartDirective?.update('none');
  }

  private updateRatesChart() {
    this.ratesService.loadAll().subscribe(() => {

      const history = this.ratesService.getRateHistory();
      const allDates = [...new Set(history.map(e => e.date))]
                    .sort((a, b) => a < b ? -1 : 1);

      const forexAssets = ASSETS.filter(a => !['GOLD', 'SILVER', 'USOIL'].includes(a));

      const datasets = forexAssets.map(asset => {
        const assetHistory = history.filter(e => e.asset === asset);
        let lastRate: number | null = null;
        const data = allDates.map(date => {
          const entry = assetHistory.find(e => e.date === date);
          if (entry) lastRate = entry.interestRate;
          return lastRate;
        });
        return {
          label: asset,
          data,
          borderColor: this.RATE_COLORS[asset] ?? '#ffffff',
          fill: false,
          pointRadius: 0,
          tension: 0,
          borderWidth: 1.5,
          spanGaps: true
        };
      });

      this.chartData = { labels: allDates, datasets } as any;
      this.cdr.markForCheck();
      this.chartDirective?.update('none');
    });
  }

  selectAsset(asset: Asset) {
    this.isRatesView.set(false);
    this.selectedAsset.set(asset);
    const data = this.cotService.getByAsset(asset);
    if (data.length > 0) this.updateChartData(data);
  }

  selectRates() {
    this.isRatesView.set(true);
    this.selectedAsset.set(null);
    this.updateRatesChart();
  }

  ngAfterViewInit() {
    this.selectedAsset.set('EUR');
    this.cotService.loadAll().subscribe(() => {
      const asset = this.selectedAsset();
      if (asset != null) {
        const data = this.cotService.getByAsset(asset);
        this.updateChartData(data);
      }
    });
  }
}
