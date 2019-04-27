package br.erlangms.rest;

import javax.persistence.Entity;

public interface IRestApiEntity {
	public void copyFromEntity(Entity obj);
}
