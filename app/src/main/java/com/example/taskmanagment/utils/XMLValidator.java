package com.example.taskmanagment.utils;

import android.content.Context;
import android.util.Log;

import com.example.taskmanagment.R;

import org.xml.sax.SAXException;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Classe utilitaire pour valider les fichiers XML contre leurs schémas XSD
 * Utilise Apache Xerces pour supporter la validation sur Android
 */
public class XMLValidator {
    private static final String TAG = "XMLValidator";

    /**
     * Valide un fichier XML contre un schéma XSD
     *
     * @param context Le contexte Android
     * @param xmlInputStream Le flux du fichier XML à valider
     * @param xsdResourceId L'ID de la ressource du schéma XSD (R.raw.users_schema)
     * @return true si le XML est valide, false sinon
     */
    public static boolean validateXML(Context context, InputStream xmlInputStream, int xsdResourceId) {
        try {
            // IMPORTANT : Forcer l'utilisation de Xerces sur Android
            System.setProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI,
                    "org.apache.xerces.jaxp.validation.XMLSchemaFactory");

            // Créer le SchemaFactory avec Xerces
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Charger le schéma XSD depuis les ressources
            InputStream xsdInputStream = context.getResources().openRawResource(xsdResourceId);
            StreamSource xsdSource = new StreamSource(xsdInputStream);
            Schema schema = schemaFactory.newSchema(xsdSource);

            // Créer le validateur
            Validator validator = schema.newValidator();

            // Valider le XML
            StreamSource xmlSource = new StreamSource(xmlInputStream);
            validator.validate(xmlSource);

            Log.i(TAG, "✅ XML validation successful");
            xsdInputStream.close();

            return true;

        } catch (SAXException e) {
            Log.e(TAG, "❌ XML validation failed (structure error): " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error during XML validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Valide le fichier users.xml
     */
    public static boolean validateUsersXML(Context context, InputStream xmlInputStream) {
        Log.d(TAG, "Validating users.xml against users_schema.xsd");
        return validateXML(context, xmlInputStream, R.raw.users_schema);
    }

    /**
     * Valide le fichier tasks.xml
     */
    public static boolean validateTasksXML(Context context, InputStream xmlInputStream) {
        Log.d(TAG, "Validating tasks.xml against tasks_schema.xsd");
        return validateXML(context, xmlInputStream, R.raw.tasks_schema);
    }
}