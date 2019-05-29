package com.lichao.jvm;

/**
 * describe: 对象在finalize函数复活
 *
 * @author lichao
 * @date 2019/05/15
 */
public class CanReliveObj {

    public static CanReliveObj obj;

    @Override
    protected void finalize() throws Throwable{
        super.finalize();
        System.out.println("CanReliveObj finalize is called");
        obj = this;
    }

    @Override
    public String toString(){
        return "I am CanReliveObj";
    }

    public static void main(String [] args){

    }
}
