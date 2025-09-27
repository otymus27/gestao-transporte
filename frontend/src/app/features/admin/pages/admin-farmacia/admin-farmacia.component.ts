// src/app/features/farmacia/farmacia-explorer/farmacia-explorer.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AdminService,
  PastaCompletaDTO,
  ArquivoAdmin,
} from '../../services/admin.service';

@Component({
  selector: 'app-farmacia-explorer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-farmacia.component.html',
  styleUrls: ['./admin-farmacia.component.scss'],
})
export class FarmaciaExplorerComponent implements OnInit {
  pasta: PastaCompletaDTO | null = null;
  subPastas: PastaCompletaDTO[] = [];
  arquivos: ArquivoAdmin[] = [];
  loading = false;

  // 🔹 Fixe o ID da pasta Farmácia
  private readonly FARMACIA_PASTA_ID = 1; // ajuste para o ID real da pasta no seu BD

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.carregarFarmacia();
  }

  carregarFarmacia(): void {
    this.loading = true;
    this.adminService.getPastaPorId(this.FARMACIA_PASTA_ID).subscribe({
      next: (detalhada) => {
        this.pasta = detalhada;
        this.subPastas = detalhada.subPastas ?? [];
        this.arquivos = detalhada.arquivos ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar pasta Farmácia:', err);
        this.loading = false;
      },
    });
  }
}
