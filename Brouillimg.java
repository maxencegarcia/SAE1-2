import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger; 
import java.util.concurrent.atomic.AtomicLong;    
import java.util.stream.IntStream;

public class Brouillimg {

    public static void main(String[] args) throws IOException { // Point d'entree du programme, peut lancer des IOExceptions

        if (args.length < 2) { // Verifie qu'il y a au moins deux arguments (image et cle)
            System.err.println("Usage: java Brouillimg <image_claire> <cle> [image_sortie]"); // Affiche l\u2019usage en cas d\u2019erreur
            System.exit(1);   // Quitte le programme car il manque des parametres
        }

        String inPath = args[0]; // Recupere le chemin de l'image d'entree
        String outPath = (args.length >= 3) ? args[2] : "out.png"; // Recupere le chemin de sortie ou met "out.png" par defaut

        int key = Integer.parseInt(args[1]) & 0x7FFF; // Convertit la cle en int et force à rester sur 15 bits via un masque binaire

        BufferedImage inputImage = ImageIO.read(new File(inPath)); // Charge l'image en memoire depuis le fichier
        if (inputImage == null) { // Verifie que le format est reconnu
            throw new IOException("Format d\u2019image non reconnu: " + inPath); // Erreur si ImageIO ne reconnaet pas l'image
        }

        final int height = inputImage.getHeight(); // Stocke la hauteur de l'image
        final int width = inputImage.getWidth();   // Stocke la largeur de l'image
        System.out.println("Dimensions de l'image : " + width + "x" + height); // Affiche les dimensions

        int[][] inputImageGL = rgb2gl(inputImage); // Convertit l\u2019image en niveaux de gris et stocke dans une matrice

        int[] perm = generatePermutation(height, key); // Genere une permutation des lignes basee sur la cle (actuellement identite)

        BufferedImage scrambledImage = scrambleLines(inputImage, perm); // Applique le melange des lignes selon la permutation

        ImageIO.write(scrambledImage, "png", new File(outPath)); // ecrit l'image resultante dans un fichier PNG
        System.out.println("Image ecrite: " + outPath); // Message de confirmation
    }

    // Convertit une image en RGB vers une matrice 2D en niveaux de gris
    public static int[][] rgb2gl(BufferedImage inputRGB) {

        final int height = inputRGB.getHeight(); // Hauteur de l'image
        final int width = inputRGB.getWidth();   // Largeur de l'image
        int[][] outGL = new int[height][width];  // Cree une matrice pour stocker les niveaux de gris

        for (int y = 0; y < height; y++) { // Parcours des lignes
            for (int x = 0; x < width; x++) { // Parcours des colonnes

                int argb = inputRGB.getRGB(x, y); // Recupere la valeur du pixel ARGB

                int r = (argb >> 16) & 0xFF; // Extrait la composante rouge
                int g = (argb >> 8) & 0xFF;  // Extrait la composante verte
                int b = argb & 0xFF;         // Extrait la composante bleue

                int gray = (r * 299 + g * 587 + b * 114) / 1000; // Calcule un niveau de gris (sans float, ponderation realiste)

                outGL[y][x] = gray; // Stocke la valeur dans la matrice
            }
        }
        return outGL; // Retourne l\u2019image convertie en gris
    }

    // Genere une permutation des lignes selon une cle
    public static int[] generatePermutation(int size, int key) {

        int[] scrambleTable = new int[size]; // Cree un tableau de taille = nombre de lignes

        for (int i = 0; i < size; i++){
            scrambleTable[i] = i; // Remplit le tableau avec l'identite (0,1,2,...)
            
        } 

        return scrambleTable; // Retourne la permutation (actuellement aucune permutation)
    }

    // Melange les lignes d\u2019une image selon la permutation
    public static BufferedImage scrambleLines(BufferedImage inputImg, int[] perm){

        int width = inputImg.getWidth();   // Largeur de l\u2019image
        int height = inputImg.getHeight(); // Hauteur de l\u2019image

        if (perm.length != height) throw new IllegalArgumentException("Taille d'image <> taille permutation"); // Verifie coherence

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); // Cree une nouvelle image vide

        // *** Rien n\u2019est fait ici : les lignes ne sont pas copiees dans l\u2019image de sortie ***
        // Cette fonction doit normalement copier inputImg.getRGB(...) selon l'ordre de perm.

        return out; // Retourne une image vide
    }

    public static int scrambledId(int id, int size, int key) {
        String keyb = Integer.toBinaryString(key);
        if(keyb.length()>16){
            System.out.println("clé trop grande");
            System.exit(0);
        }
        if(keyb.length()<16){
            while(keyb.length()<16){
                keyb = "0" + keyb;
            }
        }
        String r = keyb.substring(0, 9);
        String s = keyb.substring(9);
        int rd = Integer.parseInt(r, 2);
        int sd = Integer.parseInt(s, 2);
        id = (rd+(2*sd+1)*id)%size;
        return id;
    }
}
