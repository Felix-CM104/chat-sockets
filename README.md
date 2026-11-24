# 💬 Chat cliente-servidor con sockets

Chat de consola en **Java** con un servidor **multihilo** que admite varios clientes a la vez.
Proyecto de la asignatura **Programación de Servicios y Procesos** (2º DAM): sockets TCP, hilos y concurrencia.

## ✨ Funcionalidades

- Varios clientes conectados a la vez (un hilo por cliente con `ExecutorService`)
- Apodos únicos y validados
- Mensajes públicos con hora
- Comandos:

| Comando | Descripción |
|---|---|
| `/ayuda` | Muestra los comandos |
| `/lista` | Usuarios conectados |
| `/privado <apodo> <mensaje>` | Mensaje privado |
| `/nick <nuevo>` | Cambiar de apodo |
| `/salir` | Salir del chat |

- Avisos cuando alguien entra, sale o cambia de apodo
- Uso de `ConcurrentHashMap` y métodos `synchronized` para evitar condiciones de carrera

## 🧱 Estructura

```
src/chat/
├── Servidor.java          # Acepta conexiones y reparte los mensajes
├── ManejadorCliente.java  # Hilo que atiende a cada cliente
└── Cliente.java           # Cliente de consola (hilo receptor + hilo emisor)
```

## ▶️ Cómo ejecutarlo

Requisitos: **JDK 17 o superior**.

```bash
# Terminal 1: arranca el servidor (puerto 5000 por defecto)
bash servidor.sh

# Terminales 2, 3...: conecta clientes
bash cliente.sh                  # localhost:5000
bash cliente.sh 192.168.1.20     # otro equipo de la red
```

Desde IntelliJ IDEA: ejecuta primero `chat.Servidor` y después una o varias instancias de `chat.Cliente`
(activa *Allow multiple instances* en la configuración de ejecución).

## 🛠️ Tecnologías

Java 17 · Sockets TCP · Hilos · `ExecutorService` · Concurrencia
