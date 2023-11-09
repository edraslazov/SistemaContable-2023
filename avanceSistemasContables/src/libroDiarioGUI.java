/*********************************************************************** LIBRO DIARIO************************************************************************************/
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class Transaccion {
	 private Date fecha;
	    private String descripcion;
	    private double debe;
	    private double haber;
	    private String cuenta;
	    private int codigoCatalogo;

	    public Transaccion(Date fecha, String cuenta, int codigoCatalogo, String descripcion, double debe, double haber) {
	        this.fecha = fecha;
	        this.cuenta = cuenta;
	        this.codigoCatalogo = codigoCatalogo;
	        this.descripcion = descripcion;
	        this.debe = debe;
	        this.haber = haber;
	    }

	    public Date getFecha() {
	        return fecha;
	    }

	    public String getCuenta() {
	        return cuenta;
	    }

	    public int getCodigoCatalogo() {
	        return codigoCatalogo;
	    }

	    public String getDescripcion() {
	        return descripcion;
	    }

	    public double getDebe() {
	        return debe;
	    }

	    public double getHaber() {
	        return haber;
	    }
}

class libroDiario {
	private ArrayList<Transaccion> transacciones;
	private Date fechaMasAntigua;

    public libroDiario() {
        transacciones = new ArrayList<>();
        fechaMasAntigua = new Date(); // Establecer la fecha mas antigua como la fecha actual por defecto
    }

    public void agregarTransaccion(Transaccion transaccion) {
        transacciones.add(transaccion);
    }

    public ArrayList<Transaccion> getTransacciones() {
        return transacciones;
    }

    public Date obtenerFechaMasAntigua() {
        if (transacciones.isEmpty()) {
            return null; // Devuelve null si no hay transacciones
        }
        ArrayList<Date> fechas = new ArrayList<>();
        for (Transaccion transaccion : transacciones) {
            fechas.add(transaccion.getFecha());
        }
        Collections.sort(fechas);
        return fechas.get(0);
    }

    public void actualizarFechaMasAntigua(Date fecha) {
        if (fechaMasAntigua == null || fecha.before(fechaMasAntigua)) {
            fechaMasAntigua = fecha;
        }
    }
}

public class libroDiarioGUI {
    private JFrame frame;
    private JTable tablaContenido;
    private DefaultTableModel modeloTabla;
    private libroDiario libroDiario;
    private JComboBox<String> comboBoxCuentas;

    public libroDiarioGUI() {
        libroDiario = new libroDiario();
        prepararInterfazUsuario();
    }

    private void prepararInterfazUsuario() {
        frame = new JFrame("Libro Diario");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            modeloTabla = new DefaultTableModel(new Object[]{"Fecha", "Cuenta", "Codigo de Catalogo", "Descripcion", "Debe", "Haber"}, 0);
            tablaContenido = new JTable(modeloTabla);

            JScrollPane scrollPane = new JScrollPane(tablaContenido);
            frame.add(scrollPane);

            JPanel panelBotones = new JPanel();

            JButton botonTransaccion = new JButton("Agregar Transaccion");
            botonTransaccion.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialogoTransaccion();
                     comboBoxCuentas = new JComboBox<>();
        obtenerCuentasDesdeBD(comboBoxCuentas);
        actualizarTabla(); // Actualiza la tabla inicialmente
                }
            });

            JButton botonVerBD = new JButton("Ver Base de Datos");
            botonVerBD.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mostrarBaseDeDatos();
                }
            });
            
            //creacion del boton del libro mayor
            JButton botonVerLibroMayor = new JButton("Ver Libro Mayor");
            botonVerLibroMayor.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    verLibroMayor();
                }
            });
            
            JButton botonBalanceComprobacion = new JButton("Balance de comprobacion");
        botonBalanceComprobacion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarBalanceDeComprobacion();
            }
        });
            
            panelBotones.add(botonVerLibroMayor);
            panelBotones.add(botonTransaccion);
            panelBotones.add(botonVerBD);
            panelBotones.add(botonBalanceComprobacion);

            frame.add(panelBotones, BorderLayout.SOUTH);
            frame.setVisible(true);

            comboBoxCuentas = new JComboBox<>();
            obtenerCuentasDesdeBD(comboBoxCuentas);
    }
    
    private void mostrarBalanceDeComprobacion() {
    JFrame ventanaBalance = new JFrame("Balance de Comprobación");
    ventanaBalance.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    ventanaBalance.setPreferredSize(new Dimension(800, 600));
    
    DefaultTableModel modeloTablaBalance = new DefaultTableModel(new Object[]{"Cuenta", "Debe", "Haber"}, 0);
    JTable tablaBalance = new JTable(modeloTablaBalance);
    
    // 3. Calcular los saldos para el balance:
    HashMap<String, double[]> saldos = new HashMap<>(); // Mapa para almacenar saldos. La clave es el nombre de la cuenta y el valor es un arreglo con [debe, haber]
    for (Transaccion transaccion : libroDiario.getTransacciones()) {
        String cuenta = transaccion.getCuenta();
        double debe = transaccion.getDebe();
        double haber = transaccion.getHaber();

        if (!saldos.containsKey(cuenta)) {
            saldos.put(cuenta, new double[]{0, 0});
        }
        saldos.get(cuenta)[0] += debe;
        saldos.get(cuenta)[1] += haber;
    }

    // 4. Mostrar saldos en la ventana:
    for (Map.Entry<String, double[]> entry : saldos.entrySet()) {
        modeloTablaBalance.addRow(new Object[]{entry.getKey(), entry.getValue()[0], entry.getValue()[1]});
    }
    
    JScrollPane scrollPaneBalance = new JScrollPane(tablaBalance);
    ventanaBalance.add(scrollPaneBalance);
    
    ventanaBalance.pack();
    ventanaBalance.setVisible(true);
}
private boolean iniciarSesion() {
    String usuarioCorrecto = "usuario";
    String contrasenaCorrecta = "admin";

    while (true) {
        JTextField usuarioField = new JTextField(15);
        JPasswordField contrasenaField = new JPasswordField(15);
        
        // Estilizar los campos de texto
        usuarioField.setFont(new Font("Arial", Font.PLAIN, 18));
        contrasenaField.setFont(new Font("Arial", Font.PLAIN, 18));

        // Crear un panel personalizado para una mejor disposición y estética
        JPanel panelSesion = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(20, 20, 20, 20); // Margen para los componentes

        // Agregar componentes al panel
        constraints.gridy = 0;
        constraints.gridx = 0;
        panelSesion.add(new JLabel("Usuario:"), constraints);
        
        constraints.gridx = 1;
        panelSesion.add(usuarioField, constraints);

        constraints.gridy = 1;
        constraints.gridx = 0;
        panelSesion.add(new JLabel("Password:"), constraints);

        constraints.gridx = 1;
        panelSesion.add(contrasenaField, constraints);

        int option = JOptionPane.showConfirmDialog(null, panelSesion, "Inicio de sesion", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String usuarioIngresado = usuarioField.getText();
            String contrasenaIngresada = new String(contrasenaField.getPassword());

            if (usuarioIngresado.equals(usuarioCorrecto) && contrasenaIngresada.equals(contrasenaCorrecta)) {
                return true; // Inicio de sesion exitoso
            } else {
                JOptionPane.showMessageDialog(null, "Credenciales incorrectas. Intentalo de nuevo.", "Error de inicio de sesion", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            return false; // El usuario cancela el inicio de sesion
        }
    }
}

  private void mostrarInterfazUsuario() {
        frame = new JFrame("Libro Diario");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.getContentPane().setBackground(new Color(230, 230, 230)); // Un fondo más claro

        modeloTabla = new DefaultTableModel(new Object[]{"Fecha", "Cuenta", "Codigo de Catalogo", "Descripcion", "Debe", "Haber"}, 0);
        tablaContenido = new JTable(modeloTabla);
        tablaContenido.setFillsViewportHeight(true); // Llena el viewport
        tablaContenido.setRowHeight(30); // Aumenta la altura de las filas para una mejor legibilidad

        JScrollPane scrollPane = new JScrollPane(tablaContenido);
        frame.add(scrollPane, BorderLayout.CENTER); // Agrega el scrollPane en el centro

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 20)); // Espaciado entre botones
        panelBotones.setBackground(new Color(210, 210, 210)); // Un fondo ligeramente más oscuro para el panel de botones

        JButton botonTransaccion = new JButton("Agregar Transacción");
        botonTransaccion.setPreferredSize(new Dimension(200, 40));

        JButton botonVerBD = new JButton("Ver Base de Datos");
        botonVerBD.setPreferredSize(new Dimension(200, 40));

        JButton botonBalanceComprobacion = new JButton("Balance de comprobación");
        botonBalanceComprobacion.setPreferredSize(new Dimension(200, 40));
        botonBalanceComprobacion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarBalanceDeComprobacion();
            }
        });

        JButton botonVerLibroMayor = new JButton("Ver Libro Mayor");
        botonVerLibroMayor.setPreferredSize(new Dimension(200, 40));
        botonVerLibroMayor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verLibroMayor();
            }
        });

        panelBotones.add(botonVerLibroMayor);
        panelBotones.add(botonTransaccion);
        panelBotones.add(botonVerBD);
        panelBotones.add(botonBalanceComprobacion);

        frame.add(panelBotones, BorderLayout.SOUTH);
        frame.pack(); 
        frame.setVisible(true);

        comboBoxCuentas = new JComboBox<>();
        obtenerCuentasDesdeBD(comboBoxCuentas);
    }

        private void verLibroMayor() {
            // Crear una nueva ventana para mostrar el Libro Mayor
        JFrame ventanaLibroMayor = new JFrame("Libro Mayor");
        ventanaLibroMayor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventanaLibroMayor.setPreferredSize(new Dimension(800, 600));

        // Crear un modelo de tabla para mostrar los datos del Libro Mayor
        DefaultTableModel modeloTablaLibroMayor = new DefaultTableModel(new Object[]{"Cuenta", "Saldo"}, 0);
        JTable tablaLibroMayor = new JTable(modeloTablaLibroMayor);

        // Agregar las transacciones del Libro Diario al Libro Mayor
        for (Transaccion transaccion : libroDiario.getTransacciones()) {
            String cuenta = transaccion.getCuenta();
            double debe = transaccion.getDebe();
            double haber = transaccion.getHaber();
            
            // Realizar los cálculos para actualizar el saldo de la cuenta
            // Esto dependerá de la lógica específica de tu aplicación
            // Aquí, simplemente sumaremos el debe y el haber como ejemplo
            double saldoActualizado = debe - haber;

            // Agregar una fila al modelo de tabla del Libro Mayor con la cuenta y el saldo actualizado
            modeloTablaLibroMayor.addRow(new Object[]{cuenta, saldoActualizado});
        }

        // Crear un JScrollPane para la tabla del Libro Mayor
        JScrollPane scrollPaneLibroMayor = new JScrollPane(tablaLibroMayor);
        ventanaLibroMayor.add(scrollPaneLibroMayor);

        // Asegurarse de que la ventana se redimensione según su contenido
        ventanaLibroMayor.pack();

        // Mostrar la ventana del Libro Mayor
        ventanaLibroMayor.setVisible(true);
    }

    private int buscarCodigoCatalogo(String cuentaSeleccionada) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int codigoCatalogo = -1; // Valor predeterminado en caso de que no se encuentre

        try {
            Class.forName("org.postgresql.Driver");
            String dbURL = "jdbc:postgresql://localhost:5432/catalogoCuentas";
            String username = "postgres";
            String password = "171429";
            connection = DriverManager.getConnection(dbURL, username, password);

            String query = "SELECT idcodigocatalogo FROM tbl_catalogo WHERE descripcioncatalogo = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, cuentaSeleccionada);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                codigoCatalogo = resultSet.getInt("idcodigocatalogo");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return codigoCatalogo;
    }

    private void dialogoTransaccion() {
        JDialog dialogo = new JDialog(frame, "Agregar Transaccion", true);
        dialogo.setLayout(new GridLayout(6, 2));

        dialogo.add(new JLabel("Fecha (dd-MM-yyyy):"));
        JTextField campoFecha = new JTextField();
        dialogo.add(campoFecha);

        dialogo.add(new JLabel("Cuenta:"));
        dialogo.add(comboBoxCuentas);

        dialogo.add(new JLabel("Descripcion:"));
        JTextField campoDescripcion = new JTextField();
        dialogo.add(campoDescripcion);

        dialogo.add(new JLabel("Debe:"));
        JTextField campoDebe = new JTextField();
        dialogo.add(campoDebe);

        dialogo.add(new JLabel("Haber:"));
        JTextField campoHaber = new JTextField();
        dialogo.add(campoHaber);

        JButton agregarBoton = new JButton("Agregar");

        agregarBoton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String descripcion = campoDescripcion.getText();
                    String fechaString = campoFecha.getText();
                    String debeString = campoDebe.getText();
                    String haberString = campoHaber.getText();

                    if (descripcion.isEmpty() || fechaString.isEmpty() || debeString.isEmpty() || haberString.isEmpty()) {
                        JOptionPane.showMessageDialog(dialogo, "Todos los campos deben estar completos.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!validarFormatoFecha(campoFecha.getText())) {
                        JOptionPane.showMessageDialog(dialogo, "El formato de fecha debe ser dd-MM-yyyy.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Date fecha = analizarFecha(fechaString);
                    Date fechaMasAntigua = libroDiario.obtenerFechaMasAntigua();

                    if (fechaMasAntigua != null && fecha.before(fechaMasAntigua)) {
                        JOptionPane.showMessageDialog(dialogo, "Ingrese una fecha igual o posterior a la mas antigua registrada.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (fechaMasAntigua == null || fecha.after(fechaMasAntigua)) {
                        libroDiario.actualizarFechaMasAntigua(fecha);
                    }

                    String cuentaSeleccionada = (String) comboBoxCuentas.getSelectedItem();
                    int codigoCatalogoSeleccionado = buscarCodigoCatalogo(cuentaSeleccionada);

                    double debe = Double.parseDouble(debeString);
                    double haber = Double.parseDouble(haberString);

                    if (debe < 0 || haber < 0) {
                        JOptionPane.showMessageDialog(dialogo, "Los valores de Debe y Haber deben ser mayores o iguales a cero.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Transaccion transaccion = new Transaccion(fecha, cuentaSeleccionada, codigoCatalogoSeleccionado, descripcion, debe, haber);
                    libroDiario.agregarTransaccion(transaccion);
                    actualizarTabla();
                    dialogo.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialogo, "Ingrese valores numericos validos para Debe y Haber.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialogo, "Ingrese datos validos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialogo.add(agregarBoton);
        dialogo.pack();
        dialogo.setVisible(true);
    }

    private boolean validarFormatoFecha(String fecha) {
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yyyy");
        formatoFecha.setLenient(false); 
        try {
            formatoFecha.parse(fecha);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void obtenerCuentasDesdeBD(JComboBox<String> comboBoxCuentas) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            Class.forName("org.postgresql.Driver");
            String dbURL = "jdbc:postgresql://localhost:5432/catalogoCuentas";
            String username = "postgres";
            String password = "171429";
            connection = DriverManager.getConnection(dbURL, username, password);

            String query = "SELECT descripcioncatalogo FROM tbl_catalogo";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String cuenta = resultSet.getString("descripcioncatalogo");
                comboBoxCuentas.addItem(cuenta);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void mostrarBaseDeDatos() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            Class.forName("org.postgresql.Driver");
            String dbURL = "jdbc:postgresql://localhost:5432/catalogoCuentas";
            String username = "postgres";
            String password = "171429";
            connection = DriverManager.getConnection(dbURL, username, password);

            String query = "SELECT descripcioncatalogo, idcodigocatalogo FROM tbl_catalogo";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            // Ventana para mostrar los datos de la base de datos
            JFrame ventanaBD = new JFrame("Datos de la Base de Datos");
            ventanaBD.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            ventanaBD.setPreferredSize(new Dimension(600, 600));

            DefaultTableModel modeloTablaBD = new DefaultTableModel(new Object[]{"Cuenta", "Codigo de Catalogo"}, 0);
            JTable tablaBD = new JTable(modeloTablaBD);

            while (resultSet.next()) {
                String cuenta = resultSet.getString("descripcioncatalogo");
                int codigoCatalogo = resultSet.getInt("idcodigocatalogo");
                modeloTablaBD.addRow(new Object[]{cuenta, codigoCatalogo});
            }

            JScrollPane scrollPaneBD = new JScrollPane(tablaBD);
            ventanaBD.add(scrollPaneBD);

            ventanaBD.pack();
            ventanaBD.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yyyy");

        double totalDebe = 0;
        double totalHaber = 0;

        for (Transaccion transaccion : libroDiario.getTransacciones()) {
            modeloTabla.addRow(new Object[]{
                    formatoFecha.format(transaccion.getFecha()),
                    transaccion.getCuenta(),
                    transaccion.getCodigoCatalogo(),
                    transaccion.getDescripcion(),
                    transaccion.getDebe(),
                    transaccion.getHaber()
            });

            totalDebe += transaccion.getDebe();
            totalHaber += transaccion.getHaber();
        }

        modeloTabla.addRow(new Object[]{
                "Total",
                "",
                "",
                "",
                totalDebe,
                totalHaber,
        });
    }

    private Date analizarFecha(String dateString) {
        try {
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yyyy");
            return formatoFecha.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return new Date();
        }
    }
    
   public static void main(String[] args) {
        libroDiarioGUI programa = new libroDiarioGUI();

        if (programa.iniciarSesion()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    programa.frame.setVisible(true);  // Solo muestra la ventana si iniciarSesion() es exitoso
                }
            });
        } else {
            System.exit(0);
        }
    }
}


















