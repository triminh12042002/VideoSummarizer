CREATE DATABASE IF NOT EXISTS `developments`;

CREATE USER IF NOT EXISTS 'developments'@'%' IDENTIFIED BY 'password';

GRANT ALL ON developments.* TO 'developments'@'%';