import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InterestRatesService } from '../../core/services/interest-rates.service';
import { InterestRate } from '../../models/interest-rate.model';

@Component({
  selector: 'app-interest-rates',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './interest-rates.component.html',
  styleUrl: './interest-rates.component.scss'
})
export class InterestRatesComponent implements OnInit {

  private ratesService = inject(InterestRatesService);
  rates = signal<InterestRate[]>([]);

  ngOnInit() {
    this.ratesService.getRates().subscribe(data => this.rates.set(data));
  }

}
