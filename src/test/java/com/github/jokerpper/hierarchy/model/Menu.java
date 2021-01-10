package com.github.jokerpper.hierarchy.model;

import lombok.Data;

import java.util.List;

@Data
public class Menu {
    private Integer id;
    private String name;
    private Integer pid;
    private Integer sort;
    private List<Menu> children;
}