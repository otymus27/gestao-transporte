-- Criação da tabela motoristas
CREATE TABLE tb_motorista (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              matricula VARCHAR(20) NOT NULL UNIQUE,
                              nome VARCHAR(100) NOT NULL,
                              telefone VARCHAR(20) NOT NULL,
                              ativo BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO tb_motorista (nome, matricula, telefone, ativo) VALUES
                                                         ('ADRIANO SANTOS', '01394819', '6191999-1111',1),
                                                         ('AILTON JOSE DE SOUZA', '01202170', '6191999-1111',1),
                                                         ('ALEXANDRE VENCESLAU DE ARAUJO', '01414100', '6191999-1111',1),
                                                         ('CARLOS AUGUSTO CANDIDO', '01364057', '6191999-1111',1),
                                                         ('DANIEL MORAIS DA SILVA', '14383330', '6191999-1111',1),
                                                         ('EDILSON NUNES DA SILVA', '1363700', '6191999-1111',1),
                                                         ('EDSON ENEAS OLIVEIRA DOS SANTOS','14387808','6191999-1111',1),
                                                         ('ERIC LOPES DE OLIVEIRA','16640128','6191999-1111',1),
                                                         ('FABIO DE SOUSA LIMA', '14384841', '6191999-1111',1),
                                                         ('GERJARD MAURER','01474707','6191999-1111',1),
                                                         ('FRANCISCO MOCIENE CUNHA DE SOUSA', '14011425', '6191999-1111',1),
                                                         ('JANIO LUIZ DA SILVA AGUIAR', '14384515', '6191999-1111',1),
                                                         ('JOSE ABELARDO FIUZA OLIVEIRA','01394797','6191999-1111',1),
                                                         ('JOSE DAVID PEREIRA FARIAS', '14383128', '6191999-1111',1),
                                                         ('JOSE GERLANDIO DE PAIVA SILVA', '14385295', '6191999-1111',1),
                                                         ('LEAO MAGNO CARLOS DOS SANTOS', '1411721', '6191999-1111',1),
                                                         ('LUIZ CLAUDIO CUSTODIO MACIEL', '14383063', '6191999-1111',1),
                                                         ('LUIZ FONSECA EUFRASIO', '01363492', '6191999-1111',1),
                                                         ('MARCIO EMIDIO DA SILVA MELO', '14385015', '6191999-1111',1),
                                                         ('MAYCO ROCHA DOS SANTOS', '01403265', '6191999-1111',1),
                                                         ('NERIVALDO CARDOSO DE SOUZA', '1438423X', '6191999-1111',1),
                                                         ('PAULINO NEVES CARDOSO', '14384329', '6191999-1111',1),
                                                         ('THIAGO DE JESUS LANA', '14384221', '6191999-1111',1),
                                                         ('WESLEY GONÇALVES DA COSTA', '17237637', '6191999-1111',1),
                                                         ('WILACE LINO DA SILVA', '14401517', '6191999-1111',1);
