DROP TABLE IF EXISTS tb_all_type_entity;
DROP TABLE IF EXISTS tb_crypto_entity;

CREATE TABLE tb_all_type_entity (id int NOT NULL AUTO_INCREMENT, device json DEFAULT NULL, entities json DEFAULT NULL, status tinyint DEFAULT '0', executes json DEFAULT NULL) ENGINE=InnoDB;
CREATE TABLE tb_crypto_entity (id int NOT NULL AUTO_INCREMENT, crypto_value varchar(128) DEFAULT NULL) ENGINE=InnoDB;