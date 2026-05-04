import { Component, AfterViewInit, inject, signal, ViewChild, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, Chart, registerables } from 'chart.js';
import { CotService } from '../../core/services/cot.service';
import { Asset, ASSETS } from '../../models/asset.model';
import { CotNetData } from '../../models/cot.model';

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
  selector: 'app-cot-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './cot-chart.component.html',
  styleUrl: './cot-chart.component.scss'
})
export class CotChartComponent implements AfterViewInit {

  private cotService = inject(CotService);
  private cdr = inject(ChangeDetectorRef);

  assets = ASSETS;
  selectedAsset = signal<Asset>('EUR');
  selectedRates = signal<boolean>(false);

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
          callback: (v: any) => Math.abs(v) >= 1000 ? (v / 1000).toFixed(0) + 'k' : v
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