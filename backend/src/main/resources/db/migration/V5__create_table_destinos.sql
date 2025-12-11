CREATE TABLE tb_destino (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            nome VARCHAR(255) NOT NULL UNIQUE
);

-- Dados iniciais
INSERT INTO tb_destino (nome) VALUES
                                  ('ADMC - POO 700'),
                                  ('DIVERSOS - CASA'),
                                  ('FARMACIA ALTO CUSTO'),
                                  ('OFICINA'),
                                  ('POSTO GASOLINA'),
                                  ('LACEN'),
                                  ('HRBZ'),
                                  ('HBDF'),
                                  ('HRC'),
                                  ('HRG'),
                                  ('HMIB'),
                                  ('HRP'),
                                  ('HRAN'),
                                  ('HRS'),
                                  ('HRSAM'),
                                  ('HRSM'),
                                  ('HRT'),
                                  ('UBS 01'),
                                  ('UBS 02'),
                                  ('UBS 03'),
                                  ('UBS 04'),
                                  ('UBS 05'),
                                  ('UBS 06'),
                                  ('UBS 07'),
                                  ('UBS 08'),
                                  ('UBS 09'),
                                  ('UBS 10'),
                                  ('UBS 11'),
                                  ('UBS-ENGENHO DAS LAJES'),
                                  ('UBS-PONTE ALTA');