-- Criação da tabela carros
CREATE TABLE tb_carro (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          placa VARCHAR(10) NOT NULL UNIQUE,
                          modelo VARCHAR(50) NOT NULL,
                          marca VARCHAR(100) NOT NULL,
                          tipo VARCHAR (50) NOT NULL
);

-- Inserção de registros iniciais
INSERT INTO tb_carro (placa, modelo, marca, tipo) VALUES
                                                ('JIL1821','MASTER', 'RENAULT','FURGÃO');
