/*
 * 
 */
package br.erlangms.rest.test;

import br.erlangms.rest.discover.RestApiDiscover;

/**
 *
 * @author evertonagilar
 */
public class RestApiDiscoverTest {

    public static void main(String[] args) {
        RestApiDiscover discover;

        discover = new RestApiDiscover();

        discover.scanServices("br.ufsm.cpd.sie.academico");
        System.out.println("aqui");


    }

}
