# Battle Quiz - Trabalho de Redes de Computadores

## 📅 Cronograma

- **Apresentação em sala**: 27/11/2025
- **Envio de arquivos no SIGAA**: 30/11/2025

## 👥 Dupla - TCP
- [Bruno Nascimeno](https://github.com/Chipskein)
- [Vinicius Silva](https://github.com/Viniciusilvainfo)

## 📋 Descrição do Projeto

Este projeto consiste na implementação de um jogo em rede do tipo quiz competitivo chamado **Battle Quiz (Duelo de Conhecimento)**, desenvolvido como trabalho acadêmico para a disciplina de Redes de Computadores do Instituto Federal do Rio Grande do Sul - Campus Rio Grande.

## 🎯 Objetivo

Desenvolver uma aplicação cliente/servidor utilizando comunicação via **sockets**, reforçando os conceitos de:
- Conexões TCP ou UDP
- Troca de mensagens estruturadas
- Sincronização de estados entre processos
- Comunicação multi-cliente por meio de um servidor central

## 🏗️ Arquitetura do Sistema

### Servidor
- **Porta**: 10000 (TCP/UDP)
- **Funções principais**:
  - Aceita conexões de dois clientes/jogadores
  - Administra o jogo e valida respostas
  - Mantém banco de perguntas e alternativas
  - Controla pontuação e estado do jogo
  - Anuncia vencedor e gerencia reinícios

### Clientes (2 jogadores)
- **Funções**:
  - Enviam nicknames ao servidor
  - Recebem e exibem perguntas
  - Enviam respostas dos jogadores
  - Exibem placar e resultados das rodadas

## 🎮 Regras do Jogo

### Condições de Vitória
- Primeiro jogador a atingir **30 pontos** vence a partida

### Sistema de Pontuação
| Situação | Pontos |
|----------|--------|
| Primeiro jogador responde e acerta | +5 pontos |
| Primeiro jogador responde e erra | +3 pontos para o adversário |
| Ambos erram | 0 pontos |

### Como Rodar

  * Server

    ```bash
      mvn clean install
      java -jar target/server-1.0.0-jar-with-dependencies.jar
    ```

## ⚙️ Requisitos Técnicos

### Linguagem de Programação
- Livre (C, C++, Python, Java, Go, C#, etc.)
- Deve utilizar sockets padrão
- **Proibido** uso de bibliotecas de "jogo prontos" ou frameworks automáticos

### Demonstração
- Servidor em máquina diferente dos clientes
- Troca entre jogadores funcional
- Execução de pelo menos 3 rodadas
- Encerramento correto ou reinício da partida

