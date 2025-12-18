import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger; 
import java.util.concurrent.atomic.AtomicLong;    
import java.util.stream.IntStream;

public class Brouillimg {
    static final Scanner input = new Scanner(System.in); 

    private static String choixx;
    private static String methodes;

    public static void main(String[] args) throws IOException {

        if (args.length < 2) {
            System.err.println("Usage: java Brouillimg <image_claire> <cle> [image_sortie]"); 
            System.out.println("si vous n'avez pas la clé rentrez un nombre au hasard a la place il faut juste qu'il soit inferieur a  32768");
            System.exit(1);
        }

        String inPath = args[0];
        String outPath = (args.length >= 3) ? args[2] : "out.png";

        int key = Integer.parseInt(args[1]) & 0x7FFF;

        BufferedImage inputImage = ImageIO.read(new File(inPath)); 
        if (inputImage == null) {
            throw new IOException("Format d\u2019image non reconnu: " + inPath); 
        }

        final int height = inputImage.getHeight();
        final int width = inputImage.getWidth();
        System.out.println("Dimensions de l'image : " + width + "x" + height); 

        int[][] inputImageGL = rgb2gl(inputImage); 
        demchoix();
        
        String choix = recupchoix();
        int keyfinal = key;
        if (choix.contentEquals("decryptage")) {
            if (!hasKey()) {
                demmethode();
                String methode = recupmethode();
                if (methode.contentEquals("Euclide")||methode.contentEquals("euclide")){
                    keyfinal = breakKey(inputImage);
                    key = keyfinal;
                    System.out.println("la bonne clé est " + key);
                }
                if(methode.contentEquals("Pearson")||methode.contentEquals("pearson")){
                    keyfinal = breakKey(inputImage);
                    key = keyfinal;
                    System.out.println("la bonne clé est " + key);
                }
            }
        }
        int[] perm = generatePermutation(height, key, choix); 
        BufferedImage scrambledImage = scrambleLines(inputImage, perm); 

        ImageIO.write(scrambledImage, "png", new File(outPath)); 
        System.out.println("Image ecrite: " + outPath); 
    }

    public static boolean hasKey() {
    System.out.println("Avez-vous la clé ? (oui / non)");
    while (true) {
        String r = input.nextLine();
        if (r.equals("oui")) return true;
        if (r.equals("non")) return false;
    }
    }

    public static void demchoix(){
        String choix=" ";
        Boolean vérife=false;
        while(vérife==false) {
                System.out.println("choisissez cryptage ou decryptage");
                choix = input.nextLine();
                if(choix.contentEquals("cryptage")||choix.contentEquals("decryptage")) {
                    vérife=true;
                }
        }
        choixx = choix;
    }

    public static void demmethode(){
        System.out.println("choissiser votre méthode : (Euclide / Pearson)");
        String methode = input.nextLine();
        methodes = methode;
    }

    public static String recupchoix() {
    return choixx;
    }

    public static String recupmethode() {
    return methodes;
    }



    public static int[][] rgb2gl(BufferedImage inputRGB) {

        final int height = inputRGB.getHeight(); 
        final int width = inputRGB.getWidth();   
        int[][] outGL = new int[height][width];  

        for (int y = 0; y < height; y++) { 
            for (int x = 0; x < width; x++) { 

                int argb = inputRGB.getRGB(x, y); 
                int r = (argb >> 16) & 0xFF; 
                int g = (argb >> 8) & 0xFF;  
                int b = argb & 0xFF;         

                int gray = (r * 299 + g * 587 + b * 114) / 1000; 

                outGL[y][x] = gray; 
            }
        }
        return outGL; 
    }

    
    public static int[] generatePermutation(int size, int key, String choix) {
        int[] scrambleTable = new int[size];
        for (int i = 0; i < size; i++) {
            //on récupère le tableau mélangé et on inverse les lignes, selon le tableau.
        if (choix.contentEquals("cryptage")|choix.contentEquals("decryptage")) {
            if (choix.contentEquals("cryptage")) {
                scrambleTable[i] = scrambledId(i, size, key, choix);
            }
            if(choix.contentEquals("decryptage")){
                int coe = scrambledId(i, size, key, choix);
                scrambleTable[i] = unscrambledId(i, size, key,coe);
                            

            }
        }
    }

        return scrambleTable;
    }

    public static BufferedImage scrambleLines(BufferedImage inputImg, int[] perm) {
        int width = inputImg.getWidth();
        int height = inputImg.getHeight();
        if (perm.length != height) {
            throw new IllegalArgumentException("Taille d'image <> taille permutation");
        }
        
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < height; y++) {
            int destY = perm[y];
            for (int x = 0; x < width; x++) {
                int pixel = inputImg.getRGB(x, y);
                out.setRGB(x, destY, pixel);
            }
        }
        
        return out;
    }

    public static int pgcd(int a, int b){
        while(b!=0){
            int tmp = a%b;
            a=b;
            b=tmp;
        }
        return a;
    }

    // ---------------------------------------------- decryptage


    public static int unscrambledId(int id, int size, int key, int coe){
        String keyb = Integer.toBinaryString(key);
        
        if (keyb.length()>16) {
            System.out.println("trop grande");
            System.exit(0);
        }
        while (keyb.length()<16) {
            keyb = "0" +keyb;
        }
        String r = keyb.substring(0, 9);
        int rd = Integer.parseInt(r, 2);
        int invcoe = invaf(coe, size);
        int ido = (invcoe * ((id - rd + size) % size)) % size;
        return ido;
    }



    public static int invaf(int a, int size) {
        int sizesave = size;
        int useless = 0, invmod = 1;
        if (size == 1)
            return 0;

        while (a > 1) {
            int q = a / size;
            int tmp = size;
            size = a % size;
            a = tmp;
            tmp = useless;
            useless = invmod - q * useless;
            invmod = tmp;
        }

        if (invmod < 0)
            invmod += sizesave;

        return invmod;
    }

    public static int coecr(int coe, int size){
        while (pgcd(coe, size)>1 && coe<size){
            coe=coe+2; // pour garder l'impaire du coeff
        }
        return coe;
    }


    public static int scrambledId(int id, int size, int key, String choix) {
        String keyb = Integer.toBinaryString(key);
        
        if (keyb.length() > 16) {
            System.out.println("clé trop grande");
            System.exit(0);
        }
        
        // Padding avec des zéros à gauche pour avoir 16 bits
        while (keyb.length() < 16) {
            keyb = "0" + keyb;
        }
        
        // Extraire r (9 premiers bits) et s (7 derniers bits)
        String r = keyb.substring(0, 9);
        String s = keyb.substring(9);
        int rd = Integer.parseInt(r, 2);
        int sd = Integer.parseInt(s, 2);
        
        int coe = 2*sd+1;

        if (pgcd(coe, size)>1) {
            int ocoe = coe;
            
            coe = coecr(ocoe, size);

            if(coe>=size){
                coe=ocoe;
                while(pgcd(coe,size) >1 && coe>1){
                    coe = coe -2; // on check l'autre coter de la liste
                }
            }
        }
        id = (rd +coe * id) %size;        
        
        if (choix.contentEquals("cryptage")) {
            return id;
        }
        if(choix.contentEquals("decryptage")){
            return coe;
        }
        else{return 1;}
        
    }

// ------------------------------------------------------euclide-------------------------------------------------------------

    public static int[][] applyPermutationToGL(int[][] imageGL, int[] perm) {
    int height = imageGL.length;
    int width = imageGL[0].length;

    if (perm.length != height) {
        throw new IllegalArgumentException("Taille image != taille permutation");
    }

    int[][] out = new int[height][width];

    for (int y = 0; y < height; y++) {
        int destY = perm[y];
        if(destY < 0 || destY >= height) continue; // ignore ou gérer
        for (int x = 0; x < width; x++) {
            out[destY][x] = imageGL[y][x];
        }
    }
    return out;
}

    public static double euclideanDistance(int[] x, int[] y) {
    double sum = 0;
    for (int i = 0; i < x.length; i++) {
        double d = x[i] - y[i];
        sum += d * d;
    }
    return Math.sqrt(sum);
}

public static double scoreEuclidean(int[][] imageGL) {
    
    double totalScore = 0.0;
    for (int i = 0; i < imageGL.length - 1; i++) {
        double distance = euclideanDistance(imageGL[i], imageGL[i + 1]);
        totalScore = totalScore + distance;
    }
    
    return totalScore;
}

//--------------------------------------------------------pearson


public static double pearsonCorrelation(int[] x, int[] y) {
        int xmoyen=0;
        int ymoyen=0;
        //on fait les 3 boucle pour les sommes et on obtien la formule Σ[(xi - x̄)(yi - ȳ)]  /  sqrt( Σ(xi - x̄)² * Σ(yi - ȳ)² )
        for (int i = 0; i < x.length; i++) {
            xmoyen += x[i];
        }
        for (int i = 0; i < y.length; i++) {
            ymoyen += y[i];
        }
        xmoyen = xmoyen/x.length;
        ymoyen = ymoyen/y.length;
        //Sépare le calcule en trois partie, pour pouvoir faire les trois sommes séparément puis faire le calcule en entier
        double premièrepartie=0;
        double deuxièmepartie=0;
        double troisièmepartie=0;
        for (int i = 0; i < x.length; i++) {
            premièrepartie+=(x[i]-xmoyen)*(y[i]-ymoyen);
        }
        for (int i = 0; i < x.length; i++) {
            deuxièmepartie+=(x[i]-xmoyen)*(x[i]-xmoyen);
        }
        for (int i = 0; i < y.length; i++) {
            troisièmepartie+=(y[i]-ymoyen)*(y[i]-ymoyen);
        }
        double coefficient=0;
        coefficient= premièrepartie/Math.sqrt(deuxièmepartie*troisièmepartie);
        return coefficient;
    }
public static double scorePearson(int[][] image){
    double score=0;
    //on ajoute chaque score de chaqe ligne de l'image pour avoir le score total de l'image
    for(int y=0;y<image.length-1;y++){
        score+=pearsonCorrelation(image[y],image[y+1]);
    }
    return score;
    }


// --------------------------------------------------break key
public static int breakKey(BufferedImage scrambledImage) throws IOException {
    String methode = recupmethode();
    final int height = scrambledImage.getHeight();
    int[][] scrambledGL = rgb2gl(scrambledImage);
    int bestKey = 0;
    String choix = recupchoix();
    
    
    if (methode.contentEquals("Euclide")||methode.contentEquals("euclide")){
        double bestScore = Double.MAX_VALUE;

        for (int key = 0; key <= 32768; key++) {
            int[] inversePerm = generatePermutation(height, key, choix);
            int[][] unscrambledGL = applyPermutationToGL(scrambledGL, inversePerm);
            double score = scoreEuclidean(unscrambledGL);
            if (score < bestScore) {
                bestScore = score;
                bestKey = key;
                System.out.println("meilleur : " + bestKey);
            }
        }
    }
    if(methode.contentEquals("Pearson") || methode.contentEquals("pearson")){
        double bestScore = 0;
        for (int key = 0; key <= 32768; key++) {
            int[] inversePerm = generatePermutation(height, key, choix);
            int[][] unscrambledGL = applyPermutationToGL(scrambledGL, inversePerm);
            double score = scorePearson(unscrambledGL);
            if (score > bestScore) {
                bestScore = score;
                bestKey = key;
            }
        }
    }
    return bestKey;
    }
}


