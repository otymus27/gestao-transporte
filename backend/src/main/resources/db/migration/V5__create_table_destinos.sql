CREATE TABLE tb_destino (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            nome VARCHAR(255) NOT NULL UNIQUE
);

-- Dados iniciais
INSERT INTO tb_destino (nome) VALUES
                                  ('HRT'),
                                  ('HRG'),
                                  ('HRBZ'),
                                  ('HBDF'),
                                  ('HRC'),
                                  ('HRSM'),
                                  ('HRSAM'),
                                  ('HMIB'),
                                  ('HRAN'),
                                  ('HRS'),
                                  ('HRP'),
                                  ('UBS 01'),
                                  ('UBS 02'),
                                  ('UBS 03'),
                                  ('LACEN'),
                                  ('ADMC - POO 700'),
                                  ('UBS PONTE ALTA');