package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor de chat multihilo. Cada cliente se atiende en su propio hilo.
 *
 * Uso: java chat.Servidor [puerto]
 */
public class Servidor {

    public static final int PUERTO_POR_DEFECTO = 5000;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    /** Clientes conectados indexados por apodo. Es seguro para varios hilos. */
    private final Map<String, ManejadorCliente> clientes = new ConcurrentHashMap<>();
    private final ExecutorService hilos = Executors.newCachedThreadPool();
    private final int puerto;
    private volatile ServerSocket serverSocket;

    public Servidor(int puerto) {
        this.puerto = puerto;
    }

    public void iniciar() throws IOException {
        serverSocket = new ServerSocket(puerto);
        log("Servidor escuchando en el puerto " + puerto);
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                hilos.execute(new ManejadorCliente(socket, this));
            }
        } catch (IOException e) {
            if (!serverSocket.isClosed()) throw e;
        } finally {
            hilos.shutdownNow();
        }
    }

    public void detener() throws IOException {
        if (serverSocket != null) serverSocket.close();
        clientes.values().forEach(ManejadorCliente::cerrar);
    }

    /** Registra el apodo si está libre. Devuelve false si ya existe. */
    boolean registrar(String apodo, ManejadorCliente cliente) {
        return clientes.putIfAbsent(apodo.toLowerCase(), cliente) == null;
    }

    void eliminar(String apodo) {
        if (apodo != null && clientes.remove(apodo.toLowerCase()) != null) {
            difundir("*** " + apodo + " ha salido del chat", null);
            log(apodo + " desconectado (" + clientes.size() + " conectados)");
        }
    }

    boolean cambiarApodo(String viejo, String nuevo, ManejadorCliente cliente) {
        if (!registrar(nuevo, cliente)) return false;
        clientes.remove(viejo.toLowerCase());
        return true;
    }

    /** Envía un mensaje a todos los clientes excepto al remitente (si no es null). */
    void difundir(String mensaje, ManejadorCliente remitente) {
        for (ManejadorCliente c : clientes.values()) {
            if (c != remitente) c.enviar(mensaje);
        }
    }

    boolean enviarPrivado(String destino, String mensaje) {
        ManejadorCliente c = clientes.get(destino.toLowerCase());
        if (c == null) return false;
        c.enviar(mensaje);
        return true;
    }

    String listaConectados() {
        StringBuilder sb = new StringBuilder();
        clientes.values().forEach(c -> sb.append(sb.length() == 0 ? "" : ", ").append(c.getApodo()));
        return clientes.size() + " conectados: " + sb;
    }

    static String hora() {
        return LocalTime.now().format(HORA);
    }

    static void log(String texto) {
        System.out.println("[" + hora() + "] " + texto);
    }

    public static void main(String[] args) throws IOException {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : PUERTO_POR_DEFECTO;
        new Servidor(puerto).iniciar();
    }
}
