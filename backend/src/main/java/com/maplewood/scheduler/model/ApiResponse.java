package com.maplewood.scheduler.model;

public record ApiResponse<T>(String message, T data, boolean success) {}
