package es.udc.redes.tutorial.info;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class Info {

    public static void main(String[] args) throws IOException {
        if (args.length ==0 )
            return;
        File file = new File(args[0]);
        BasicFileAttributes atributos= Files.readAttributes(file.toPath(), BasicFileAttributes.class);

        try {
            System.out.println("Tamano: " + atributos.size() + " B");
            System.out.println("Ultima modificacion: " + atributos.lastModifiedTime());
            System.out.println("Nombre: " + file.getName());
            System.out.println("Extension: " + Info.getExtension(file.getName()));
            System.out.println("Tipo: " + (atributos.isDirectory() ? "Directorio" : (atributos.isRegularFile() ? "Fichero" : (atributos.isOther() ? "Otro" : "Link Simbolico"))));
            System.out.println("Ruta absoluta: " + file.getAbsolutePath());
        } finally {

        }

    }
    public static String getExtension(String file){
        int i = file.lastIndexOf('.');
        return i > 0 ? file.substring(i+1) : "";
    }

}
