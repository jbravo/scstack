/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: UsuarioActivadoResource.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.service.restlets.users;

import es.sidelab.scstack.lib.exceptions.SCStackException;
import es.sidelab.scstack.lib.exceptions.api.ExcepcionLogin;
import es.sidelab.scstack.lib.exceptions.api.ExcepcionParametros;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionGestorLDAP;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPNoExisteRegistro;
import es.sidelab.scstack.service.data.Usuario;
import es.sidelab.scstack.service.restlets.BaseProyectosResource;
import es.sidelab.scstack.service.restlets.BaseUsuariosResource;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;


/**
 * <p>Recurso que permite bloquear/desactivar y desbloquear/activar usuarios de la Forja.</p>
 * <p>Sobre este recurso está permitido realizar métodos PUT y DELETE.</p>
 * @author Arek Klauza
 */
public class UsuarioActivadoResource extends BaseUsuariosResource {

	private static final Logger LOGGER = Logger.getLogger(BaseProyectosResource.class.getName());
	
    public UsuarioActivadoResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
    }


    /**
     * <p>PUT</p>
     * <p>Activa un usuario que previamente estaba bloqueado. O en caso de que
     * estuviese activado, simplemente cambia su contraseña. Si el UID no
     * existe devuelve un 404.</p>
     * <p>Corresponde con el CUS.3</p>
     * @param entity Representación del objeto que se recibe en la petición
     * @throws ResourceException
     */
    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        Usuario usuario = null;
        String user = this.userReq;
        String pass = this.passReq;

        try {
            // Decodificamos la información en función de cómo venga la petición
            usuario = this.decodificaUsuario(entity);

            // Si el uid de la URI no coincide con el uid del XML se aborta
            if (!((String) getRequest().getAttributes().get("uid")).equals(usuario.getUid())) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "El uid suministrado en la estructura de datos no coincide con el de la URI");
            }

            // Guarda el nuevo usuario a través del proxy que habla con la API
            proxy.activarUsuario(usuario.getUid(), usuario.getPass(), user, pass);

            // Devuelve en el location header de respuesta la URI del nuevo usuario
            getResponse().setStatus(Status.SUCCESS_OK);
            getResponse().setLocationRef("/" + usuario.getUri());

        } catch (JSONException ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionParametros ex) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ex.getMessage());
        } catch (ExcepcionLogin ex) {
            // this.pedirLogin(ex.getMessage());
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, ex.getMessage());
        } catch (ExcepcionLDAPNoExisteRegistro ex) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, ex.getMessage());
        } catch (ExcepcionGestorLDAP ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (IOException ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (SCStackException e) {
        	LOGGER.log(Level.SEVERE,"Exception",e);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage());
		}
    }
    @Override
    public boolean allowPut() {
        return true;
    }

    /**
     * <p>DELETE</p>
     * <p>Bloquea/Desactiva un usuario determinado de la Forja. Lo hace mediante
     * el borrado de su contraseña, bloqueando así su acceso a la Forja.</p>
     * <p>Corresponde con el CUS.2</p>
     * @throws ResourceException
     */
    @Override
    public void removeRepresentations()throws ResourceException {
        String user = this.userReq;
        String pass = this.passReq;

        try {
            String uid = (String)getRequest().getAttributes().get("uid");
            proxy.desactivarUsuario(uid, user, pass);

            // Notifica que la solicitud ha sido realizada. No necesita enviarse ningún contenido.
            getResponse().setStatus(Status.SUCCESS_OK);

        } catch (ExcepcionParametros ex) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ex.getMessage());
        } catch (ExcepcionLogin ex) {
            // this.pedirLogin(ex.getMessage());
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, ex.getMessage());
        } catch (ExcepcionLDAPNoExisteRegistro ex) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, ex.getMessage());
        } catch (ExcepcionGestorLDAP ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (SCStackException e) {
        	LOGGER.log(Level.SEVERE,"Exception",e);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage());
		}
    }
    @Override
    public boolean allowDelete() {
        return true;
    }
}
