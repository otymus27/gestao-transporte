import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-autocomplete-teste',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatAutocompleteModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  template: `
    <mat-form-field appearance="outline" class="w-100">
      <mat-label>Exemplo</mat-label>
      <input
        type="text"
        matInput
        [(ngModel)]="valor"
        [matAutocomplete]="auto"
        [ngModelOptions]="{ standalone: true }"
      />
      <mat-autocomplete #auto="matAutocomplete">
        <mat-option *ngFor="let item of opcoes" [value]="item">
          {{ item }}
        </mat-option>
      </mat-autocomplete>
    </mat-form-field>
  `,
})
export class AutocompleteTesteComponent {
  valor = '';
  opcoes = ['Financeiro', 'RH', 'TI', 'Compras'];
}
