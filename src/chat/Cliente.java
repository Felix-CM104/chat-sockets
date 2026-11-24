package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Cliente de chat por consola. Usa dos hilos:
 *  - uno que escucha los mensajes del servidor y los muestra,
 *  - el principal, que lee lo que escribe el usuario y lo envía.
 *
 * Uso: java chat.Cliente [host] [puerto]
 */
public class Cliente {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int puerto = args.length > 1 ? Integer.parseInt(args[1]) : Servidor.PUERTO_POR_DEFECTO;

        try (Socket socket = new Socket(host, puerto);
             BufferedReader delServidor = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter alServidor = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {

            Thread receptor = new Thread(() -> {
                try {
                    String linea;
                    while ((linea = delServidor.readLine()) != null) {
                        System.out.println(linea);
                    }
                } catch (IOException ignored) {
                    // socket cerrado
                }
                System.out.println("Conexión cerrada por el servidor.");
                System.exit(0);
            });
            receptor.setDaemon(true);
            receptor.start();

            String linea;
            while ((linea = teclado.readLine()) != null) {
                alServidor.println(linea);
                if (linea.trim().equalsIgnoreCase("/salir")) {
                    receptor.join(2000);
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo conectar con " + host + ":" + puerto + " (" + e.getMessage() + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
