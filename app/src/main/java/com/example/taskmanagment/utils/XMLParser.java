package com.example.taskmanagment.utils;

import android.util.Log;

import com.example.taskmanagment.models.Task;
import com.example.taskmanagment.models.TaskPriority;
import com.example.taskmanagment.models.TaskStatus;
import com.example.taskmanagment.models.User;
import com.example.taskmanagment.models.UserType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Classe utilitaire pour parser les fichiers XML
 * Utilise DOM (Document Object Model) pour la lecture
 */
public class XMLParser {
    private static final String TAG = "XMLParser";

    /**
     * Parse un fichier XML contenant des utilisateurs
     */
    public static List<User> parseUsersXML(InputStream inputStream) {
        List<User> users = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            NodeList userNodes = document.getElementsByTagName("user");

            for (int i = 0; i < userNodes.getLength(); i++) {
                Node userNode = userNodes.item(i);

                if (userNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) userNode;
                    User user = parseUserElement(userElement);
                    users.add(user);
                }
            }

            Log.d(TAG, "Successfully parsed " + users.size() + " users");

        } catch (Exception e) {
            Log.e(TAG, "Error parsing users XML: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Parse un élément User du XML
     */
    private static User parseUserElement(Element element) {
        User user = new User();

        user.setId(getElementText(element, "id"));
        user.setUsername(getElementText(element, "username"));
        user.setPassword(getElementText(element, "password"));
        user.setUserType(UserType.fromString(getElementText(element, "userType")));
        user.setEmail(getElementText(element, "email"));
        user.setFullName(getElementText(element, "fullName"));

        String createdDate = getElementText(element, "createdDate");
        if (createdDate != null && !createdDate.isEmpty()) {
            user.setCreatedDate(Long.parseLong(createdDate));
        }

        return user;
    }

    /**
     * Parse un fichier XML contenant des tâches
     */
    public static List<Task> parseTasksXML(InputStream inputStream) {
        List<Task> tasks = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            NodeList taskNodes = document.getElementsByTagName("task");

            for (int i = 0; i < taskNodes.getLength(); i++) {
                Node taskNode = taskNodes.item(i);

                if (taskNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element taskElement = (Element) taskNode;
                    Task task = parseTaskElement(taskElement);
                    tasks.add(task);
                }
            }

            Log.d(TAG, "Successfully parsed " + tasks.size() + " tasks");

        } catch (Exception e) {
            Log.e(TAG, "Error parsing tasks XML: " + e.getMessage());
            e.printStackTrace();
        }

        return tasks;
    }

    /**
     * Parse un élément Task du XML
     */
    private static Task parseTaskElement(Element element) {
        Task task = new Task();

        task.setId(getElementText(element, "id"));
        task.setTitle(getElementText(element, "title"));
        task.setDescription(getElementText(element, "description"));
        task.setAssignedTo(getElementText(element, "assignedTo"));
        task.setCreatedBy(getElementText(element, "createdBy"));
        task.setStatus(TaskStatus.fromString(getElementText(element, "status")));

        String priority = getElementText(element, "priority");
        if (priority != null && !priority.isEmpty()) {
            task.setPriority(TaskPriority.fromString(priority));
        }

        String createdDate = getElementText(element, "createdDate");
        if (createdDate != null && !createdDate.isEmpty()) {
            task.setCreatedDate(Long.parseLong(createdDate));
        }

        String dueDate = getElementText(element, "dueDate");
        if (dueDate != null && !dueDate.isEmpty()) {
            task.setDueDate(Long.parseLong(dueDate));
        }

        String completedDate = getElementText(element, "completedDate");
        if (completedDate != null && !completedDate.isEmpty()) {
            task.setCompletedDate(Long.parseLong(completedDate));
        }

        return task;
    }

    /**
     * Récupère le texte d'un élément enfant
     */
    private static String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node != null && node.getFirstChild() != null) {
                return node.getFirstChild().getNodeValue();
            }
        }
        return "";
    }
}