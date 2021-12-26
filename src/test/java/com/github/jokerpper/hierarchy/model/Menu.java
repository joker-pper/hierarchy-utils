package com.github.jokerpper.hierarchy.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Menu implements Serializable {
    private static final long serialVersionUID = -4665872900962945145L;

    private Integer id;
    private String name;
    private Integer pid;
    private Integer sort;
    private List<Menu> children;
}