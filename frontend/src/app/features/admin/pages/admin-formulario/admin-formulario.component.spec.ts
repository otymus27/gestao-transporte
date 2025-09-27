import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminFormularioComponent } from './admin-formulario.component';

describe('AdminFormularioComponent', () => {
  let component: AdminFormularioComponent;
  let fixture: ComponentFixture<AdminFormularioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminFormularioComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminFormularioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
