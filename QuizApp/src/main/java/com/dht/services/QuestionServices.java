/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dht.services;

import com.dht.pojo.Choice;
import com.dht.pojo.Level;
import com.dht.pojo.Question;
import com.dht.utils.JdbcConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Result;

/**
 *
 * @author admin
 */
public class QuestionServices {
    public void addQuestion(Question q) throws SQLException {
        Connection conn = JdbcConnector.getInstance().connect();
        
        conn.setAutoCommit(false);
        
        String sql = "INSERT INTO question(content, hint, image, category_id, level_id) VALUES(?, ?, ?, ?, ?)";
        PreparedStatement stm = conn.prepareCall(sql);
        stm.setString(1, q.getContent());
        stm.setString(2, q.getHint());
        stm.setString(3, q.getImage());
        stm.setInt(4, q.getCategory().getId());
        stm.setInt(5, q.getLevel().getId());
        
        if (stm.executeUpdate() > 0) {
            int questionId = -1;
            ResultSet rs = stm.getGeneratedKeys();
            if (rs.next())
                questionId = rs.getInt(1);
            
            sql = "INSERT INTO choice(content, is_correct, question_id) VALUES(?, ?, ?)";
            stm = conn.prepareCall(sql);
            for (var c: q.getChoices()) {
                stm.setString(1, c.getContent());
                stm.setBoolean(2, c.isCorrect());
                stm.setInt(3, questionId);
                stm.executeUpdate();
            }
            
            conn.commit();
        } else
            conn.rollback();
    }
    
     public List<Question> getQuestion() throws SQLException {
        Connection conn = JdbcConnector.getInstance().connect();

        Statement stm = conn.createStatement();
        ResultSet rs = stm.executeQuery("SELECT * FROM question order by id desc");

        List<Question> questions = new ArrayList<>();
        while (rs.next()) {
            Question q = new Question.Builder(rs.getInt("id"),rs.getString("content")).build();
            questions.add(q);
        }
        
        return questions;
    }
     public List<Question> getQuestion(String kw) throws SQLException {
        Connection conn = JdbcConnector.getInstance().connect();

        PreparedStatement stm = conn.prepareCall("SELECT * FROM question where  content like concat('%',?,'%') order by id desc");
       stm.setString(1,kw);
        ResultSet rs = stm.executeQuery();
        List<Question> questions = new ArrayList<>();
        while (rs.next()) {
            Question q = new Question.Builder(rs.getInt("id"),rs.getString("content")).build();
            questions.add(q);
        }
        
        return questions;
    }
      public List<Question> getQuestion(int num) throws SQLException {
        Connection conn = JdbcConnector.getInstance().connect();

        PreparedStatement stm = conn.prepareCall("SELECT * FROM questionlimit ? order by rand()");
       stm.setInt(1,num);
        ResultSet rs = stm.executeQuery();
        List<Question> questions = new ArrayList<>();
        while (rs.next()) {
            Question q = new Question.Builder(rs.getInt("id"),rs.getString("content"))
                    .addChoices((this.getChoicesByQuestion(rs.getInt("id")))).build();
            
            
            
            questions.add(q);
        }
        
        return questions;
    }
     public boolean  delQuestion(int id) throws SQLException{
           Connection conn = JdbcConnector.getInstance().connect();
           PreparedStatement stm = conn.prepareCall("DELETE FROM question where id=?");
           stm.setInt(1, id);
          return stm.executeUpdate()>0;
     }
     public List<Question> getOptions(int num) throws SQLException {
        Connection conn = JdbcConnector.getInstance().connect();

        PreparedStatement stm = conn.prepareCall("SELECT * FROM question LIMIT ? ORDER BY RAND()");
       stm.setInt(1,num);
        ResultSet rs = stm.executeQuery();
        List<Question> questions = new ArrayList<>();
        while (rs.next()) {
            Question q = new Question.Builder(rs.getInt("id"),rs.getString("content")).build();
            
            
            questions.add(q);
        }
        
        return questions;
    }
     public List<Choice> getChoicesByQuestion(int qId) throws SQLException{
           Connection conn = JdbcConnector.getInstance().connect();
           
        PreparedStatement stm = conn.prepareCall("SELECT * FROM choice where question_id=?");
       stm.setInt(1,qId);
       ResultSet rs = stm.executeQuery();
       List<Choice> choices = new ArrayList<>();
       while(rs.next()){
           Choice c = new Choice(rs.getInt("if"), rs.getString("content"),rs.getBoolean("is_correct"));
       }
       return choices;
     }
}
