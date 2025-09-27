import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminFarmaciaComponent } from './admin-farmacia.component';

describe('AdminFarmaciaComponent', () => {
  let component: AdminFarmaciaComponent;
  let fixture: ComponentFixture<AdminFarmaciaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminFarmaciaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminFarmaciaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
