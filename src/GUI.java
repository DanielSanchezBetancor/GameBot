import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class GUI {
	public static String[] texto = {"Bienvenido a BigTime Bot", "Antes de empezar, asegúrate de que la aplicacion Bluestack está al frente.", "Mientras el bot se está ejecutando, no podrás utilizar el ordenador, tenlo en cuenta.", "Presiona 'Empezar' para arrancar el bot."};
	public static int nextLineCounter = 0;
	private String[] aux = new String[4];
	private static JTextArea cuadroTextoComponente;
	private static JScrollPane cuadroTexto;
	private static JPanel panel;
	public GUI() {
	}
	public static JFrame buildGUI() {
		JFrame ventana = new JFrame();
		panel = new JPanel();
		ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ventana.setBounds(0, 0, 500, 400);
		ventana.setLocationRelativeTo(null);
		panel.setBounds(0, 0, 600, 200);
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		cuadroTextoComponente = new JTextArea();
		cuadroTextoComponente.setEditable(false);
		cuadroTextoComponente.setLineWrap(true);
		cuadroTexto = new JScrollPane(cuadroTextoComponente); 
		JButton continuar = new JButton("Continuar");
		continuar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (nextLineCounter < 4) {
					nextLine();
				}
				if (continuar.getText().equals("Empezar")) {
					continuar.setVisible(false);
					new Thread(new Runnable() {
						public void run() {
							cleanText();
							new Inicio().inicio();
							}
						}).start();
					}
				if (nextLineCounter == 4) {
					continuar.setText("Empezar");
				}
				
			}
		});
		continuar.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(cuadroTexto);
		panel.add(continuar);
		ventana.add(panel);
		return ventana;
		
	}
	public static void main(String[] args) {
		JFrame ventana = buildGUI();
		show(ventana);
	}
	public static void show(JFrame ventana) {
		ventana.setVisible(true);
	}
	public static void nextLine() {
		if (nextLineCounter != 0)
			cuadroTextoComponente.append("\n");
		cuadroTextoComponente.append(texto[nextLineCounter]);
		nextLineCounter++;
	}
	public static void addText(String text) {
		cuadroTextoComponente.append(text);
		cuadroTextoComponente.append("\n");
		JScrollBar sb = cuadroTexto.getVerticalScrollBar();
		sb.setValue( sb.getMaximum() );
	}
	public static void cleanText() {
		cuadroTextoComponente.setText("");
	}
}
