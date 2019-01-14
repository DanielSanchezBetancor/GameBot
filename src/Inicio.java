import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import javax.swing.SwingUtilities;

public class Inicio {
	private static int h, w, bluestacksHo, bluestacksWo, bluestacksHeight, bluestacksWidth;
	// {B, G, R}
	private static int[] coloresTopBar = {108, 90, 88 }, coloresLeftBar = {56, 33, 30 }, colorRojo = {0, 0, 180 }, colorAzul = {180, 72, 0}, colorNaranja = {23, 98, 211 }, colorVerde = {13, 180, 0 }; // BGR
	private static String rutaConfigFile = "config.ini", contact = "Por favor, contacte con @DanalaDanazo para solicitar ayuda.";
	private static Robot capturador;
	private static String[][] casillas = new String[7][7];
	private static int startingPointCounter = 1;
	
	public void inicio() {
		System.out.println("Iniciando bot");
		callGUI("Iniciando bot.");
		try {
			capturador = new Robot();
			Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
			h = (int) pantalla.getHeight();
			w = (int) pantalla.getWidth();
			callGUI("Buscando el eje de coordenadas 0,0 para el proceso Bluestack");
			long start = System.nanoTime();
			getBluestacksDimensions();
			callGUI("Coordenadas encontradas. ETT: " + ((System.nanoTime() - start) / 1000000000) + " segundos.");
			//System.out.println("DEBUG - bluestacskHo: " + bluestacksHo + " - bluestacskWo: " + bluestacksWo);
			callGUI("Buscando el archivo de configuracion");
			boolean existe = checkConfigFile();
			if (!existe) {
				callGUI("Calculando tamaño vertical de la aplicación");
				getBluestacksHeight();
				callGUI("Calculado.\nCalcultando tamaño horizontal de la aplicación");
				getBluestacksWidth();
				callGUI("Calculado");
				saveConfigFile();
			} else {
				callGUI("Buscando en el archivo, la altura y anchura del programa");
				setBluestacksHeightAndWeight();
				callGUI("Encontrado y aplicado.");
			}
			startingPoint();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	// Podemos suponer que, debido al tamaño de la aplicacion, cogera siempre o el punto medio de la pantalla, o alguno de los cuartos (comprobar 1/2, 1/4, 1/8)
	// vueltas referencia al dividendo de la division de pantalla, de forma recursiva (minimo siempre es 2, para 1/vueltas)
	public static void getBluestacksDimensions() {
		int[] posTopBar = getBluestacksTopBarPos(2);
		getBluestackStartingWidth(posTopBar[0]);
	}

	/*
	 * posTopBar [0] - width bluestacks top bar; posTopBar [1] - height bluestacks top bar
	 */
	public static int[] getBluestacksTopBarPos(int vueltas) {
		boolean salir = false;
		int[] posTopBar = new int[2];
		int i = w / vueltas;
		while (i < w) {
			for (int j = 0; j < h; j++) {
				if (capturador.getPixelColor(i, j).getBlue() == coloresTopBar[0] && capturador.getPixelColor(i, j).getGreen() == coloresTopBar[1] && capturador.getPixelColor(i, j).getRed() == coloresTopBar[2]) {
					posTopBar[0] = i;
					posTopBar[1] = j;
					bluestacksHo = j;
					i = w;
					j = h;
					salir = true;
				}
			}
			if (!salir)
				i += w / vueltas;
		}
		if (!salir)
			posTopBar = getBluestacksTopBarPos(vueltas * 2);
		//System.out.println("DEBUG getBluestacksTopBarPos - posTopBar: " + Arrays.toString(posTopBar));
		return posTopBar;
	}
	
	public static void getBluestackStartingWidth(int i) {
		while (capturador.getPixelColor(i, bluestacksHo).getBlue() == coloresTopBar[0] && capturador.getPixelColor(i, bluestacksHo).getGreen() == coloresTopBar[1] && capturador.getPixelColor(i, bluestacksHo).getRed() == coloresTopBar[2]) {
			i--;
		}
		bluestacksWo = i+1;
	//	System.out.println("DEBUG getBluestackStartingWidth - bluestacksWo: " + bluestacksWo + " - i: " + i);
	}

	public static boolean checkConfigFile() {
		File archivo = new File(rutaConfigFile);
		if (archivo.exists()) {
			return true;
		} else {
			return false;
		}
	}

	// Asumimos que cuando haya salido del top bar, empezara a tocar el left bar
	public static void getBluestacksHeight() {
		int i = bluestacksWo, j = bluestacksHo;
		while (capturador.getPixelColor(i, j).getBlue() == coloresTopBar[0] && capturador.getPixelColor(i, j).getGreen() == coloresTopBar[1] && capturador.getPixelColor(i, j).getRed() == coloresTopBar[2]) {
			j++;
		}
		while (capturador.getPixelColor(i, j).getBlue() == coloresLeftBar[0] && capturador.getPixelColor(i, j).getGreen() == coloresLeftBar[1] && capturador.getPixelColor(i, j).getRed() == coloresLeftBar[2]) {
			j++;
		}
		bluestacksHeight = (j - bluestacksHo) - 1; // -1 para quitar el pixel azul del borde, el ultimo a detectar
	}

	public static void getBluestacksWidth() {
		int i = bluestacksWo, j = bluestacksHo;
		while (capturador.getPixelColor(i, j).getBlue() == coloresTopBar[0] && capturador.getPixelColor(i, j).getGreen() == coloresTopBar[1] && capturador.getPixelColor(i, j).getRed() == coloresTopBar[2]) {
			i++;
		}
		bluestacksWidth = (i - bluestacksWo) - 1; // -1 para quitar el pixel azul del borde, el ultimo a detectar;
	}

	// Se guarda como un objeto int de 2 posiciones, siendo [0] la altura del programa, y [1] el ancho del programa
	public static void saveConfigFile() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(rutaConfigFile));
			int[] datos = { bluestacksHeight, bluestacksWidth };
			oos.writeObject(datos);
			oos.close();
		} catch (FileNotFoundException e) {
			try {
				File f = new File(rutaConfigFile);
				f.createNewFile();
				saveConfigFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setBluestacksHeightAndWeight() {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(rutaConfigFile));
			int[] datos = (int[]) ois.readObject();
			bluestacksHeight = datos[0];
			bluestacksWidth = datos[1];
			ois.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//Naranja inicial - r=255,g=200,b=146 | r=255,g=194,b=136 | r=255,g=195,b=140 ---> 194 < g < 202 <--- r=255,g=202,b=150
	//Verde inicial - r=116,g=213,b=156 | r=125,g=217,b=162 | r=126,g=218;b=163 | r=122,g=216,b=160 | r=120,g=215,b=158 | r=123,g=216,b=161 | r=171,g=230,b=195
	//Azul inicial r=140,g=192,b=255 | r=142,g=193,b=255 | r=143,g=194,b=255 | r=149,g=197,b=255 | r=150,g=197,b=255 ---- r=140~150 
	//| [r=171,g=209,b=255] | [r=180,g=216,b=255] | r=138,g=191,b=255 | r=136,g=191,b=255 ---> 191 < g < 216 
	//Rojo inicial - r=255,g=151,b=150 | r=255,g=148,b=147 | r=255,g=150,b=149 | r=255,g=150,b=149 --> 137 < g < 151 | r=255,g=146,b=145 | r=255,g=143,b=142 | r=255,g=137,b=136
	public static void startingPoint()  { 
		callGUI("Analizando tablero.");
		int [][] inicial = fillArray();
			for (int i = 0;i<casillas.length;i++) {
				for (int j = 0;j<casillas[i].length;j++) {
					Color color = capturador.getPixelColor(calculatePointX(i), calculatePointY(j));
					System.out.println(calculatePointX(i) + ":" + calculatePointY(j)); //DEBUG
					System.out.println("Color encontrado: " + color);
					if (color.getRed() != colorAzul[2]) {
						if (color.getBlue() != colorRojo[0]) {
							if (color.getRed() == 255) {
								if (color.getGreen() >= 194 && color.getGreen() <= 202) {
									casillas[i][j] = "Ni";
								} else if (color.getGreen() >= 137 && color.getGreen() <= 151) {
									casillas[i][j] = "Ri";
								}
								inicial[0][0] = j;
								inicial[0][1] = i;
							} else if (color.getRed() >= 116 && color.getRed() <= 171) {
								casillas[i][j] = "Vi";
								inicial[0][0] = j;
								inicial[0][1] = i;
							} else if (color.getRed() >= 136 && color.getRed() <= 150 || color.getRed() == 171 || color.getRed() == 180) {
								if (color.getGreen() >= 191 && color.getGreen() <= 216) {
									casillas[i][j] = "Ai";
									inicial[0][0] = j;
									inicial[0][1] = i;
								}
							} else {
								casillas[i][j] = "N";
							}
						} else {
							casillas[i][j] = "R";
						}
					} else {
						if (color.getGreen() != colorAzul[1]) {
							casillas[i][j] = "V";
						} else if (color.getGreen() == colorAzul[1]) {
							casillas[i][j] = "A";
						} else {
							casillas[i][j] = "R";
						}
					}
				}
			}
			pintarMapa(); //DEBUG
			callGUI("Tablero analizado.");
			try {
				looper(inicial);
			} catch (NullPointerException e) {
				callGUI("Error inesperado analizando los colores.");
				errorHandler();
			}
	}
	//px hasta llegar al programa, 62 px hasta el centro del boton, 49 px el tamaño del boton, 4 px de separacion
	public static int calculatePointX(int boton) {
		return (bluestacksWo + (62 + (49*boton) + 4*boton));
		
	}
	//px hasta llegar al programa, 241 px hasta el centro del boton, 49 px el tamaño del boton, 4 px de separacion
	public static int calculatePointY(int boton) {
		return (bluestacksHo + (246 + (49*boton) + 4*boton));
	}
	public static void looper(int[][] startingPoint) throws NullPointerException {	
		if (startingPoint[0][0] == -1 && startingPoint[0][1] == -1) {
			callGUI("Error a la hora de escoger un inicial.");
			errorHandler();
		} else {
			callGUI("Calculando movimientos.");
			String [][] temp = new String[100][3];
			int tempCounter = 0;
			boolean salir = false;
			String[] lista = new String[99]; //Se modificara cuando se pueda leer el nº en pantalla
			int listaCounter = 0;
			String[] colores = {"N", "V", "R", "A"};
			boolean[][] casillasBoolean = new boolean[7][7];
			boolean transformed = false;
			int rojoCounter = 0, naranjaCounter = 0, azulCounter = 0, verdeCounter = 0;
			while (!salir) {
				//Recorremos los colores
				for (int i = 0;i<colores.length;i++) {
					//Si no es la primera iteracion, cambiamos los nombres de vuelta al color (sin inicial)
					if (i != 0) {
						limpiarInicialesTemporales(startingPoint);
					}
					//Recorremos todas las casillas
					for (int j = 0;j<casillas.length;j++) {
						for (int x = 0;x<casillas[j].length;x++) {
							//Si la casilla ha sido utilizada antes para comprobar ese color, negamos volver a comprobar la casilla
							if (casillasBoolean[x][j] == false) {
								//Comprobamos que si en la anterior iteracion cambiamos un valor para añadirlo a casillasBoolean, se reinicie el valor
								if (transformed == true)
									transformed = false;
								//Comprobamos que sea una casilla inicial la posicion en la que estamos
								if (casillas[x][j].equals("Ni") || casillas[x][j].equals("Vi") || casillas[x][j].equals("Ri") ||casillas[x][j].equals("Ai") || 
										casillas[x][j].equals("Nt") || casillas[x][j].equals("Vt") || casillas[x][j].equals("Rt") ||casillas[x][j].equals("At")) {
									//Comprobar que no estemos en el limite
									if (x != 0) {
										//Si la casilla oeste es igual, transformamos esa casilla y subimos al contador segun el color
										if (casillas[x-1][j].equals(colores[i])) {
											casillas[x-1][j] += "t";
											temp[tempCounter][0] = colores[i];
											temp[tempCounter][1] = Integer.toString(x-1);
											temp[tempCounter][2] = Integer.toString(j);
											tempCounter++;
											transformed = true;
											if (colores[i] == "N") {
												naranjaCounter++;
											} else if (colores[i] == "V") {
												verdeCounter++;
											} else if (colores[i] == "R") {
												rojoCounter++;
											} else {
												azulCounter++;
											}
										}
									}
									//Comprobar que no estemos en el limite
									if (j != 0) {
										//Si la casilla norte es igual, transformamos esa casilla y subimos al contador segun el color
										if (casillas[x][j-1].equals(colores[i])) {
											casillas[x][j-1] += "t";
											temp[tempCounter][0] = colores[i];
											temp[tempCounter][1] = Integer.toString(x);
											temp[tempCounter][2] = Integer.toString(j-1);
											tempCounter++;
											transformed = true;
											if (colores[i] == "N") {
												naranjaCounter++;
											} else if (colores[i] == "V") {
												verdeCounter++;
											} else if (colores[i] == "R") {
												rojoCounter++;
											} else {
												azulCounter++;
											}
										}
									}
									//Comprobar que no estemos en el limite
									if (j != 6) {
										//Si la casilla sur es igual, transformamos esa casilla y subimos al contador segun el color
										if (casillas[x][j+1].equals(colores[i])) {
											casillas[x][j+1] += "t";
											temp[tempCounter][0] = colores[i];
											temp[tempCounter][1] = Integer.toString(x);
											temp[tempCounter][2] = Integer.toString(j+1);
											tempCounter++;
											transformed = true;
											if (colores[i] == "N") {
												naranjaCounter++;
											} else if (colores[i] == "V") {
												verdeCounter++;
											} else if (colores[i] == "R") {
												rojoCounter++;
											} else {
												azulCounter++;
											}
										}
									}
									//Comprobar que no estemos en el limite
									if (x != 6) {
										//Si la casilla este es igual, transformamos esa casilla y subimos al contador segun el color
										if (casillas[x+1][j].equals(colores[i])) {
											casillas[x+1][j] += "t";
											temp[tempCounter][0] = colores[i];
											temp[tempCounter][1] = Integer.toString(x+1);
											temp[tempCounter][2] = Integer.toString(j);
											tempCounter++;
											transformed = true;
											if (colores[i] == "N") {
												naranjaCounter++;
											} else if (colores[i] == "V") {
												verdeCounter++;
											} else if (colores[i] == "R") {
												rojoCounter++;
											} else {
												azulCounter++;
											}
										}
									}
								}
								//Si hemos transformado alguna casilla desde esta, la marcamos para no volver a analizar, y empezamos a analizar de 0 con las nuevas casillas
								if (transformed) {
									casillasBoolean[x][j] = true;
									j = 0;
									x = -1;
								}
							}
						}
					}
					//pintarMapa();
					casillasBoolean = limpiarMapaBooleans(casillasBoolean);
				}
				limpiarInicialesTemporales(startingPoint);
				//Calculamos que color ha tenido mas opciones de transformacion y las marcamos como iniciales
				String send = "";
				if (naranjaCounter >= verdeCounter) {
					if (naranjaCounter >= rojoCounter) {
						if (naranjaCounter >= azulCounter) {
							send = "N";
							
						} else {
							send = "A";
						}
					} else if (rojoCounter >= azulCounter) {
						send = "R";
					} else {
						send = "A";
					}
				} else if (verdeCounter >= rojoCounter) {
					if (verdeCounter >= azulCounter) {
						send = "V";
					} else {
						send = "A";
					}
				}  else if (rojoCounter >= azulCounter) {
					send = "R";
				} else {
					send = "A";
				}
				lista[listaCounter] = send;
				listaCounter++;
				startingPoint = setInicial(temp, tempCounter, send, startingPoint);
				temp = new String[100][3];
				tempCounter = 0;
				naranjaCounter = 0;
				verdeCounter = 0;
				rojoCounter = 0;
				azulCounter = 0;
				//pintarMapa();
				//Si ha terminado de convertir todas las casillas, mostrar la combinacion de colores en pantalla.
				if (end()) {
					salir = true;
					callGUI("Partida finalizada. Ejecutando movimientos.");
				}
			}
			clickar(lista);
			try {
				callGUI("Reiniciando...");
				Thread.sleep(10000);
				resetGame();
				startingPoint = fillArray();
				startingPoint();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private static void clickMenuPrincipal() {
		Robot click;
		try {
			click = new Robot();
			click.mouseMove(bluestacksWo + 225, bluestacksHo + 670);    
			click.mousePress(InputEvent.BUTTON1_MASK);
			click.mouseRelease(InputEvent.BUTTON1_MASK);
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
	}

	//280-430 espacio en blanco de los premios
	private static boolean comprobarMenu() {
		if (capturador.getPixelColor((bluestacksWo + 280), (bluestacksHo + 430)).getRGB() == new Color(255, 255, 255).getRGB()) {
			callGUI("Menu principal detectado.");
			return true;
		}
		return false;
	}

	//Como startingPoint no es global, simplementelo haremos un return para reiniciar el vector
	//return -> startingPoint
	public static void resetGame() {
		casillas = new String[7][7];
		startingPointCounter = 1;
	}
	private static void limpiarInicialesTemporales(int[][] startingPoint) {
		for (int aux = 0;aux<casillas.length;aux++) {
			for (int aux2 = 0;aux2<casillas[aux].length;aux2++) {
				if (casillas[aux2][aux].length() > 1) {
					if (casillas[aux2][aux].substring(1, 2).equals("t")) {
						casillas[aux2][aux] = casillas[aux2][aux].substring(0, 1);	
					}
				}
			}
		}		
	}

	private static boolean[][] limpiarMapaBooleans(boolean[][] casillasBoolean) {
		for (int i = 0;i<casillasBoolean.length;i++) {
			for (int j = 0;j<casillasBoolean[i].length;j++) {
				if (casillasBoolean[j][i] == true) {
					casillasBoolean[j][i] = false;
				}
			}
		}
		return casillasBoolean;
	}

	//Marcamos la casillas como iniciales
	public static int[][] setInicial(String[][] temp, int tempCounter, String color, int[][] startingPoint) {
		for (int i = 0;i<tempCounter;i++) {
			if (temp[i][0].equals(color)) {
				casillas[Integer.parseInt(temp[i][1])][Integer.parseInt(temp[i][2])] += "i";
				startingPoint[startingPointCounter][0] = Integer.parseInt(temp[i][1]);
				startingPoint[startingPointCounter][1] = Integer.parseInt(temp[i][2]);
				startingPointCounter++;
			}
		}
		return startingPoint;
	}
	//Comprobamos que todas las casillas sean iniciales
	public static boolean end() {
		boolean returned = true;
		for (int i = 0;i<casillas.length;i++) {
			for (int j = 0;j<casillas[i].length;j++) {
				if (casillas[j][i].length() == 1) {
					returned = false;
					j = casillas.length;
				}
			}
			if (!returned) {
				i = casillas.length;
			}
		}
		return returned;
	}
	public static int[][] fillArray() {
		int[][] aux = new int[49][2];
		for (int i = 0;i<aux.length;i++) {
			aux[i][0] = -1;
			aux[i][1] = -1;
		}
		return aux;
	}
	
	public static void pintarMapa() {
		for (int i = 0;i<casillas.length;i++) {
			System.out.print("{");
			for (int j = 0;j<casillas[i].length;j++) {
				if ((j+1) != casillas[i].length) {
					System.out.print(casillas[j][i] + ", ");
				} else {
					System.out.println(casillas[j][i] + "}");
				}
			}
		}
	}
	
	public static void callGUI(String texto) {
		GUI.addText(texto);
	}
	//Boton azul - 110:666 ; Boton rojo - 190:666 ; Boton verde - 270:666 ; Boton naranja - 350:666
	public static void clickar(String[] lista) {
		for (int i = 0;i<lista.length;i++) {
			int x = -1;
			if (lista[i] != null) {
				switch(lista[i]) {
					case "N": 
					x = 350;
					break;
					case "A":
					x = 110;
					break;
					case "R":
					x = 190;
					break;
					case "V":
					x = 270;
					break;
					default:
					x = -1;
					break;
				} 
				if (x == -1) {
					callGUI("Error seleccionando los colores para pulsar. " + contact);
				} else {
					x += bluestacksWo;
					try {
						Robot click = new Robot();
						click.mouseMove(x, (666 + bluestacksHo));    
						click.mousePress(InputEvent.BUTTON1_MASK);
						click.mouseRelease(InputEvent.BUTTON1_MASK);
						Thread.sleep(1000);
					} catch (AWTException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	public static boolean comprobarAnuncios() {
		boolean anuncio = false;
		int xx = -1, yx = -1;
		//Anuncio Libertex vertical
		//System.out.println(capturador.getPixelColor((bluestacksWo+426), (bluestacksHo+64)) + " x: " + (bluestacksWo+426) + " - yx: " + (bluestacksHo+64)); //DEBUG
		Color c  = new Color(222, 222, 222);
		if (capturador.getPixelColor((bluestacksWo+426), (bluestacksHo+64)).getRGB() == c.getRGB()) {
			xx = bluestacksWo + 426;
			yx = bluestacksHo + 64;
		}
		c  = new Color(221, 221, 221);
		if (capturador.getPixelColor((bluestacksWo+1360), (bluestacksHo+77)).getRGB() == c.getRGB()) {
			xx = bluestacksWo + 1360;
			yx = bluestacksHo + 77;
		}
		//Anuncio Play Store
		c  = new Color(255, 255, 255);
		if (capturador.getPixelColor((bluestacksWo+430), (bluestacksHo+59)).getRGB() == c.getRGB()) {
			xx = bluestacksWo + 430;
			yx = bluestacksHo + 59;
		}
		if (xx == -1 || yx == -1) {
		} else {
			try {
				callGUI("Encontrado anuncio. Procediendo a cerrarlo.");
				Robot click = new Robot();
				click.mouseMove(xx, yx);    
				click.mousePress(InputEvent.BUTTON1_MASK);
				click.mouseRelease(InputEvent.BUTTON1_MASK);
				callGUI("Anuncio cerrado.");
				Thread.sleep(5000);
				callGUI("Reiniciando el juego.");
				clickMenuPrincipal();
				Thread.sleep(5000);
				anuncio = true;
			} catch (AWTException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
		return anuncio;
	}
	public static void errorHandler() {
		callGUI("Comprobando la existencia de algun anuncio.");
		boolean anuncio = comprobarAnuncios();
		if (anuncio) {
			startingPoint();
		} else {
			callGUI("Comprobacion finalizada y negativa. Comprobando si es el menu principal.");
			boolean menu = comprobarMenu();
			if (menu) {
				callGUI("Comprobacion finalizada. Iniciando juego desde el menu principal.");
				clickMenuPrincipal();
				callGUI("Esperando a que el tablero este presente.");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				startingPoint();
			} else {
				callGUI("Comprobacion finalizada y negativa. Comprobando si es el la pantalla de subida de nivel.");
				if (comprobarDesbloqueoJuego()) { //170, 660
					callGUI("Comprobacion finalizada. Cerrando pantalla de subida de nivel.");
					clickDesbloqueoJuego();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					startingPoint();
				} else {
					callGUI("No se ha detectado nada. Esperando 10 segundos y volviendo a intentar...");
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					callGUI("Reiniciando deteccion.");
					errorHandler();
				}
			}
		}
	}

	private static void clickDesbloqueoJuego() {
		Robot click;
		try {
			click = new Robot();
			click.mouseMove(bluestacksWo + 170, bluestacksHo + 660);    
			click.mousePress(InputEvent.BUTTON1_MASK);
			click.mouseRelease(InputEvent.BUTTON1_MASK);
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
	}

	private static boolean comprobarDesbloqueoJuego() {
		if (capturador.getPixelColor((bluestacksWo + 170), (bluestacksHo + 660)).getRGB() == new Color(78, 46, 145).getRGB()) {
			callGUI("Detectado nivel desbloqueado.");
			return true;
		}
		return false;
	}
}