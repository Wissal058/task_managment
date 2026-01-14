package com.example.taskmanagment.utils;


import android.content.Context;
import android.util.Log;


import com.example.taskmanagment.models.Task;
import com.example.taskmanagment.models.User;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Classe utilitaire pour écrire des données dans des fichiers XML
 * Utilise DOM pour la création et la modification de documents XML
 */
public class XMLWriter {
    private static final String TAG = "XMLWriter";

    /**
     * Écrit une liste d'utilisateurs dans un fichier XML
     */
    public static boolean writeUsersToXML(Context context, List<User> users, String fileName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            // Créer l'élément racine
            Element rootElement = document.createElement("users");
            document.appendChild(rootElement);

            // Ajouter chaque utilisateur
            for (User user : users) {
                Element userElement = createUserElement(document, user);
                rootElement.appendChild(userElement);
            }

            // Écrire le document dans un fichier
            return writeDocumentToFile(context, document, fileName);

        } catch (Exception e) {
            Log.e(TAG, "Error writing users to XML: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Crée un élément XML pour un utilisateur
     */
    private static Element createUserElement(Document document, User user) {
        Element userElement = document.createElement("user");

        appendChild(document, userElement, "id", user.getId());
        appendChild(document, userElement, "username", user.getUsername());
        appendChild(document, userElement, "password", user.getPassword());
        appendChild(document, userElement, "userType", user.getUserType().name());
        appendChild(document, userElement, "email", user.getEmail());
        appendChild(document, userElement, "fullName", user.getFullName());
        appendChild(document, userElement, "createdDate", String.valueOf(user.getCreatedDate()));

        return userElement;
    }

    /**
     * Écrit une liste de tâches dans un fichier XML
     */
    public static boolean writeTasksToXML(Context context, List<Task> tasks, String fileName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            // Créer l'élément racine
            Element rootElement = document.createElement("tasks");
            document.appendChild(rootElement);

            // Ajouter chaque tâche
            for (Task task : tasks) {
                Element taskElement = createTaskElement(document, task);
                rootElement.appendChild(taskElement);
            }

            // Écrire le document dans un fichier
            return writeDocumentToFile(context, document, fileName);

        } catch (Exception e) {
            Log.e(TAG, "Error writing tasks to XML: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Crée un élément XML pour une tâche
     */
    private static Element createTaskElement(Document document, Task task) {
        Element taskElement = document.createElement("task");

        appendChild(document, taskElement, "id", task.getId());
        appendChild(document, taskElement, "title", task.getTitle());
        appendChild(document, taskElement, "description", task.getDescription());
        appendChild(document, taskElement, "assignedTo", task.getAssignedTo());
        appendChild(document, taskElement, "createdBy", task.getCreatedBy());
        appendChild(document, taskElement, "status", task.getStatus().name());
        appendChild(document, taskElement, "priority", task.getPriority().name());
        appendChild(document, taskElement, "createdDate", String.valueOf(task.getCreatedDate()));
        appendChild(document, taskElement, "dueDate", String.valueOf(task.getDueDate()));

        if (task.getCompletedDate() > 0) {
            appendChild(document, taskElement, "completedDate", String.valueOf(task.getCompletedDate()));
        }

        return taskElement;
    }

    /**
     * Ajoute un élément enfant avec du texte
     */
    private static void appendChild(Document document, Element parent, String tagName, String textContent) {
        Element element = document.createElement(tagName);
        element.setTextContent(textContent != null ? textContent : "");
        parent.appendChild(element);
    }

    /**
     * Écrit un document XML dans un fichier
     */
    private static boolean writeDocumentToFile(Context context, Document document, String fileName) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // Formatage du XML
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // Créer le fichier de sortie
            File outputFile = new File(context.getFilesDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            // Transformer le document en fichier
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(outputStream);
            transformer.transform(source, result);

            outputStream.close();

            Log.d(TAG, "Successfully wrote XML to file: " + fileName);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error writing document to file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
