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
                                  ('UBS 04'),
                                  ('UBS 05'),
                                  ('UBS 06'),
                                  ('UBS 07'),
                                  ('UBS 08'),
                                  ('UBS 09'),
                                  ('LACEN'),
                                  ('FARMACIA ALTO CUSTO'),
                                  ('ADMC - POO 700'),
                                  ('UBS-ENGENHO DAS LAJES'),
                                  ('UBS-PONTE ALTA');