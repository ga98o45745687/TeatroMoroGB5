package exp3_s8_gabrielbravo;

import java.util.*;

public class Exp3_s8_gabrielbravo {

    // — Capacidad máxima de ventas —
    private static final int MAX_SALES = 200;

    // — Estructuras de datos primitivas para ventas —
    private int[] saleIds           = new int[MAX_SALES];
    private String[] seatLocations  = new String[MAX_SALES];
    private String[] clientNames    = new String[MAX_SALES];
    private double[] saleBaseCosts  = new double[MAX_SALES];
    private double[] saleFinalCosts = new double[MAX_SALES];
    private int saleCount = 0;

    // — Estructuras dinámicas —
    private List<Promotion> promotions = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();

    // — Estadísticas globales —
    private double totalIncome = 0.0;
    private int totalTicketsSold = 0;
    private double totalDiscountGiven = 0.0;

    public static void main(String[] args) {
        Exp3_s8_gabrielbravo manager = new Exp3_s8_gabrielbravo();
        manager.initPromotions();
        manager.run();
    }

    private void initPromotions() {
        promotions.add(new Promotion("Estudiante", 0.10));
        promotions.add(new Promotion("TerceraEdad", 0.15));
    }

    public void run() {
        Scanner sc = new Scanner(System.in);
        int option;
        do {
            System.out.println("\n--- Menú Teatro Moro ---");
            System.out.println("1. Venta de entrada");
            System.out.println("2. Resumen de ventas");
            System.out.println("3. Generar boleta");
            System.out.println("4. Ingresos totales");
            System.out.println("5. Eliminar venta");
            System.out.println("6. Actualizar venta");
            System.out.println("7. Benchmark de ventas");
            System.out.println("8. Salir");
            System.out.print("Seleccione opción: ");
            option = readInt(sc);
            switch (option) {
                case 1: venderEntrada(sc); break;
                case 2: mostrarResumen();       break;
                case 3: generarBoleta(sc);      break;
                case 4: mostrarIngresos();      break;
                case 5: eliminarVenta(sc);      break;
                case 6: actualizarVenta(sc);    break;
                case 7: runBenchmark(1000);     break;
                case 8: System.out.println("¡Hasta luego!"); break;
                default: System.out.println("Opción inválida.");
            }
        } while (option != 8);
        sc.close();
    }

    /**
     * Venta de entrada con validación y registro.
     */
    private void venderEntrada(Scanner sc) {
        if (saleCount >= MAX_SALES) {
            System.out.println("Capacidad de ventas alcanzada.");
            return;
        }
        System.out.print("Nombre del cliente: ");
        String client = sc.nextLine().trim();
        if (client.isEmpty()) {
            System.out.println("Cliente inválido."); return;
        }

        System.out.println("Ubicaciones: VIP($50), Platea($30), Balcón($20)");
        System.out.print("Ingrese ubicación: ");
        String seat = sc.nextLine().trim();
        if (isSeatReserved(seat)) {
            System.out.println("La ubicación ya está reservada.");
            return;
        }

        System.out.print("Categoría (Estudiante, TerceraEdad o Ninguna): ");
        String cat = sc.nextLine().trim();

        // Crear y registrar la venta
        insertSale(client, seat, cat);
    }

    /**
     * Inserta una venta genérica 
     */
    public boolean insertSale(String client, String seat, String category) {
        if (saleCount >= MAX_SALES) return false;
        double baseCost = getBaseCost(seat);
        double rate     = getDiscountRate(category);
        double finalCost= baseCost * (1 - rate);
        int id = saleCount + 1;

        // Guardar en arreglos
        saleIds[saleCount]           = id;
        clientNames[saleCount]       = client;
        seatLocations[saleCount]     = seat;
        saleBaseCosts[saleCount]     = baseCost;
        saleFinalCosts[saleCount]    = finalCost;
        saleCount++;

        // Reservar asiento en lista dinámica
        reservations.add(new Reservation(id, client, seat));

        // Actualizar estadísticas
        totalTicketsSold++;
        totalIncome += finalCost;
        totalDiscountGiven += (baseCost - finalCost);

        System.out.printf("Venta #%d registrada: %s - %s | $%.2f (desc. %.0f%%)%n",
                id, client, seat, finalCost, rate * 100);
        return true;
    }

    /**
     * Elimina una venta 
     */
    public boolean removeSale(int id) {
        int idx = findSaleIndex(id);
        if (idx < 0) return false;

        // Ajustar estadísticas inversamente
        totalTicketsSold--;
        totalIncome -= saleFinalCosts[idx];
        totalDiscountGiven -= (saleBaseCosts[idx] - saleFinalCosts[idx]);

        // Quitar reserva dinámica
        reservations.removeIf(r -> r.saleId == id);

        // Desplazar arreglos para eliminar hueco
        for (int i = idx; i < saleCount - 1; i++) {
            saleIds[i] = saleIds[i + 1];
            clientNames[i] = clientNames[i + 1];
            seatLocations[i] = seatLocations[i + 1];
            saleBaseCosts[i] = saleBaseCosts[i + 1];
            saleFinalCosts[i] = saleFinalCosts[i + 1];
        }
        saleCount--;
        return true;
    }

    /**
     * Actualiza una venta existente 
     */
    public boolean updateSale(int id, String newSeat, String newCategory) {
        int idx = findSaleIndex(id);
        if (idx < 0) return false;
        if (isSeatReserved(newSeat)) {
            System.out.println("Nueva ubicación ya reservada.");
            return false;
        }
        // Quitar antigua reserva
        reservations.removeIf(r -> r.saleId == id);
        double oldFinal = saleFinalCosts[idx];
        double oldBase  = saleBaseCosts[idx];

        // Calcular nuevos costos
        double baseCost = getBaseCost(newSeat);
        double rate     = getDiscountRate(newCategory);
        double finalCost= baseCost * (1 - rate);

        // Actualizar arreglos
        seatLocations[idx] = newSeat;
        saleBaseCosts[idx] = baseCost;
        saleFinalCosts[idx]= finalCost;

        // Registrar nueva reserva
        reservations.add(new Reservation(id, clientNames[idx], newSeat));

        // Ajustar estadísticas
        totalIncome += (finalCost - oldFinal);
        totalDiscountGiven += ((oldBase - oldFinal) - (baseCost - finalCost));

        return true;
    }

    private boolean isSeatReserved(String seat) {
        return reservations.stream()
                .anyMatch(r -> r.seat.equalsIgnoreCase(seat));
    }

    private double getBaseCost(String loc) {
        switch (loc.toLowerCase()) {
            case "vip":     return 50.0;
            case "platea":  return 30.0;
            case "balcón":  return 20.0;
            default: System.out.println("Ubicación no reconocida, usando Platea."); return 30.0;
        }
    }

    private double getDiscountRate(String category) {
        return promotions.stream()
                .filter(p -> p.getName().equalsIgnoreCase(category))
                .map(Promotion::getRate)
                .findFirst().orElse(0.0);
    }

    private void mostrarResumen() {
        if (saleCount == 0) {
            System.out.println("No hay ventas registradas."); return;
        }
        System.out.println("\n--- Resumen de Ventas ---");
        for (int i = 0; i < saleCount; i++) {
            System.out.printf("%d) ID:%d | Cliente:%s | %s | Base:$%.2f | Final:$%.2f%n",
                    i + 1, saleIds[i], clientNames[i], seatLocations[i], saleBaseCosts[i], saleFinalCosts[i]);
        }
    }

    private void generarBoleta(Scanner sc) {
        System.out.print("Ingrese VentaID para boleta: ");
        int id = readInt(sc);
        int idx = findSaleIndex(id);
        if (idx < 0) { System.out.println("Venta no encontrada."); return; }
        System.out.println("\n--- Boleta Venta #" + id + " ---");
        System.out.println("Cliente: " + clientNames[idx]);
        System.out.println("Ubicación: " + seatLocations[idx]);
        System.out.printf("Base: $%.2f | Final: $%.2f%n",
                saleBaseCosts[idx], saleFinalCosts[idx]);
        System.out.println("Gracias por su preferencia.");
    }

    private void mostrarIngresos() {
        System.out.println("\n--- Estadísticas Globales ---");
        System.out.println("Total vendidas: " + totalTicketsSold);
        System.out.printf("Ingresos totales: $%.2f%n", totalIncome);
        System.out.printf("Descuento total otorgado: $%.2f%n", totalDiscountGiven);
        System.out.println("Reservas activas: " + reservations.size());
    }

    private void eliminarVenta(Scanner sc) {
        System.out.print("Ingrese VentaID a eliminar: ");
        int id = readInt(sc);
        if (removeSale(id)) System.out.println("Venta eliminada.");
        else System.out.println("ID no encontrado.");
    }

    private void actualizarVenta(Scanner sc) {
        System.out.print("Ingrese VentaID a actualizar: ");
        int id = readInt(sc);
        System.out.print("Nueva ubicación: ");
        String seat = sc.nextLine().trim();
        System.out.print("Nueva categoría: ");
        String cat = sc.nextLine().trim();
        if (updateSale(id, seat, cat)) System.out.println("Venta actualizada.");
        else System.out.println("Error al actualizar.");
    }

    private int findSaleIndex(int id) {
        for (int i = 0; i < saleCount; i++) if (saleIds[i] == id) return i;
        return -1;
    }

    private int readInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            System.out.print("Ingrese un número válido: "); sc.next();
        }
        int v = sc.nextInt(); sc.nextLine(); return v;
    }

    /**
     * Benchmark para medir tiempo de inserciones.
     */
    public void runBenchmark(int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations && saleCount < MAX_SALES; i++) {
            insertSale("Test" + i, "Platea", "Ninguna");
        }
        long elapsed = System.nanoTime() - start;
        System.out.printf("Benchmark: %d inserciones en %d ns (%.2f µs/op)%n",
                iterations, elapsed, elapsed / (double) iterations / 1_000);
    }

    // — Clases auxiliares —
    private static class Promotion {
        private final String name;
        private final double rate;
        public Promotion(String name, double rate) { this.name = name; this.rate = rate; }
        public String getName() { return name; }
        public double getRate() { return rate; }
    }

    private static class Reservation {
        private final int saleId;
        private final String client;
        private final String seat;
        public Reservation(int saleId, String client, String seat) {
            this.saleId = saleId; this.client = client; this.seat = seat;
        }
        @Override public String toString() {
            return String.format("[SaleID:%d, Cliente:%s, Ubicación:%s]", saleId, client, seat);
        }
    }
}

// Test
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Exp3_s8_gabrielbravoTest {

    private Exp3_s8_gabrielbravo manager;

    @BeforeEach
    void setUp() {
        manager = new Exp3_s8_gabrielbravo();
        manager.initPromotions();
    }

    @Test
    void testInsertSale() {
        assertTrue(manager.insertSale("Alice", "VIP", "Estudiante"));
        assertEquals(1, manager.totalTicketsSold);
        assertEquals(1, manager.reservations.size());
        int idx = manager.findSaleIndex(1);
        assertNotEquals(-1, idx);
        assertEquals("Alice", manager.clientNames[idx]);
    }

    @Test
    void testRemoveSale() {
        manager.insertSale("Bob", "Platea", "Ninguna");
        assertTrue(manager.removeSale(1));
        assertEquals(0, manager.totalTicketsSold);
        assertEquals(0, manager.saleCount);
    }

    @Test
    void testUpdateSale() {
        manager.insertSale("Carol", "Balcón", "Ninguna");
        assertTrue(manager.updateSale(1, "VIP", "TerceraEdad"));
        int idx = manager.findSaleIndex(1);
        assertEquals("VIP", manager.seatLocations[idx]);
    }

    @Test
    void testBenchmarksInsertMultiple() {
        manager.runBenchmark(10);
        assertTrue(manager.saleCount >= 10);
    }

    @Test
    void testInvalidRemove() {
        assertFalse(manager.removeSale(99));
    }

    @Test
    void testInvalidUpdate() {
        assertFalse(manager.updateSale(99, "Platea", "Estudiante"));
    }
}
