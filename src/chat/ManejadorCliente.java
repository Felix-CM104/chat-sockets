package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Hilo que atiende a un cliente concreto: lee sus mensajes y los procesa.
 */
public class ManejadorCliente implements Runnable {

    private final Socket socket;
    private final Servidor servidor;
    private PrintWriter salida;
    private volatile String apodo;

    ManejadorCliente(Socket socket, Servidor servidor) {
        this.socket = socket;
        this.servidor = servidor;
    }

    public String getApodo() {
        return apodo;
    }

    @Override
    public void run() {
        try (BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            salida = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            if (!pedirApodo(entrada)) return;

            enviar("Bienvenido/a, " + apodo + ". Escribe /ayuda para ver los comandos.");
            servidor.difundir("*** " + apodo + " se ha unido al chat", this);
            Servidor.log(apodo + " conectado desde " + socket.getInetAddress().getHostAddress());

            String linea;
            while ((linea = entrada.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                if (linea.startsWith("/")) {
                    if (!procesarComando(linea)) break;
                } else {
                    servidor.difundir("[" + Servidor.hora() + "] " + apodo + ": " + linea, this);
                }
            }
        } catch (IOException e) {
            // El cliente se ha desconectado de forma abrupta
        } finally {
            servidor.eliminar(apodo);
            cerrar();
        }
    }

    private boolean pedirApodo(BufferedReader entrada) throws IOException {
        enviar("Introduce tu apodo:");
        String linea;
        while ((linea = entrada.readLine()) != null) {
            String propuesto = linea.trim();
            if (!esApodoValido(propuesto)) {
                enviar("Apodo no válido (3-15 caracteres, letras, números o _). Prueba otro:");
            } else if (!servidor.registrar(propuesto, this)) {
                enviar("Ese apodo ya está en uso. Prueba otro:");
            } else {
                apodo = propuesto;
                return true;
            }
        }
        return false;
    }

    /** Devuelve false si el cliente quiere salir. */
    private boolean procesarComando(String linea) {
        String[] partes = linea.split("\\s+", 3);
        switch (partes[0].toLowerCase()) {
            case "/ayuda" -> enviar("""
                    Comandos disponibles:
                      /lista                 -> ver usuarios conectados
                      /privado <apodo> <msg> -> mensaje privado
                      /nick <nuevo>          -> cambiar de apodo
                      /salir                 -> salir del chat""");
            case "/lista" -> enviar(servidor.listaConectados());
            case "/privado" -> {
                if (partes.length < 3) {
                    enviar("Uso: /privado <apodo> <mensaje>");
                } else if (!servidor.enviarPrivado(partes[1],
                        "[" + Servidor.hora() + "] (privado) " + apodo + ": " + partes[2])) {
                    enviar("No hay nadie conectado con el apodo " + partes[1]);
                } else {
                    enviar("(privado para " + partes[1] + ") " + partes[2]);
                }
            }
            case "/nick" -> {
                if (partes.length < 2 || !esApodoValido(partes[1])) {
                    enviar("Uso: /nick <nuevo> (3-15 caracteres, letras, números o _)");
                } else if (!servidor.cambiarApodo(apodo, partes[1], this)) {
                    enviar("Ese apodo ya está en uso.");
                } else {
                    String viejo = apodo;
                    apodo = partes[1];
                    servidor.difundir("*** " + viejo + " ahora se llama " + apodo, null);
                }
            }
            case "/salir" -> {
                enviar("¡Hasta pronto!");
                return false;
            }
            default -> enviar("Comando desconocido. Escribe /ayuda");
        }
        return true;
    }

    static boolean esApodoValido(String apodo) {
        return apodo != null && apodo.matches("\\w{3,15}");
    }

    synchronized void enviar(String mensaje) {
        if (salida != null) salida.println(mensaje);
    }

    void cerrar() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
