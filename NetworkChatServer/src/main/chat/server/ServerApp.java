package main.chat.server;

import main.chat.server.chat.MyServer;

public class ServerApp {
    private static final int DEFAULT_PORT = 8189;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;


//        Значения для args можно передать в консоли, т.е. когда консольное приложение запускается
//        непосредственно через нее (обычно мы запускаем через IDEA, не через консоль).
//        Это можно использовать, если нужно запускать приложение под каким-то определенным портом:
//        значение этого порта можно передавать в качестве аргументов в консоли. И эти аргументы будут
//        учитываться в качестве значений массива args.

//        Проверяем, не пустой ли массив args (т.е. не передали ли в него какие-то значения)
        if (args.length != 0) {
//            Если args оказался не пустым, то считаем (только в рамках нашего урока), что первым
//            аргументом в массиве идет порт. Ну а поскольку значение в массиве - это тип String,
//            то его нужно привести к числовому типу.
            port = Integer.parseInt(args[0]);
        }


        new MyServer().start(port);
    }

}
