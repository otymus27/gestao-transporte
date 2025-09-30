-- Criação da tabela solicitacoes
CREATE TABLE tb_solicitacao (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                data_solicitacao DATE NOT NULL,
                                status VARCHAR(20) NOT NULL,
                                km_inicial INT,
                                km_final INT,
                                hora_saida TIME,
                                hora_chegada TIME,
                                id_carro BIGINT NOT NULL,
                                id_motorista BIGINT NOT NULL,
                                id_usuario BIGINT NOT NULL,
                                id_setor BIGINT NOT NULL,
                                id_destino BIGINT NOT NULL,
                                FOREIGN KEY (id_carro) REFERENCES tb_carro(id),
                                FOREIGN KEY (id_motorista) REFERENCES tb_motorista(id),
                                FOREIGN KEY (id_usuario) REFERENCES tb_usuarios(id),
                                FOREIGN KEY (id_setor) REFERENCES tb_setor(id),
                                FOREIGN KEY (id_destino) REFERENCES tb_destino(id)
);

-- Inserção de 5 registros iniciais
INSERT INTO tb_solicitacao (
    data_solicitacao, status, km_inicial, km_final, hora_saida, hora_chegada,
    id_carro, id_motorista, id_usuario, id_setor, id_destino
) VALUES
      ('2025-05-01', 'PENDENTE', 1, 5, '17:50:00', '18:00:00', 1, 1, 1, 1, 1),
      ('2025-05-02', 'CONCLUIDA', 1, 5, '17:50:00', '18:00:00', 2, 2, 2, 2, 2),
      ('2025-05-03', 'RECUSADA', 1, 5, '17:50:00', '18:00:00', 3, 3, 3, 3, 3),
      ('2025-05-04', 'PENDENTE', 1, 5, '17:50:00', '18:00:00', 4, 4, 4, 4, 4),
      ('2025-05-05', 'CONCLUIDA', 1, 5, '17:50:00', '18:00:00', 5, 5, 5, 5, 5);