package test;

import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

/**
 * @author Kohsuke Kawaguchi
 */
public class Myself {
    private final String dn;
    private final ConnectionFactory factory;
    public String firstName, lastName, email, userId;

    public Myself(String dn, ConnectionFactory factory) throws NamingException {
        this.dn = dn;
        this.factory = factory;

        LdapContext context = factory.connect();
        try {
            Attributes attributes = context.getAttributes(dn);
            firstName = getAttribute(attributes,"givenName");
            lastName = getAttribute(attributes,"sn");
            email = getAttribute(attributes,"mail");
            userId = getAttribute(attributes,"cn");
        } finally {
            context.close();
        }
    }

    private String getAttribute(Attributes attributes, String name) throws NamingException {
        Attribute att = attributes.get(name);
        return att!=null ? (String) att.get() : null;
    }

    public HttpResponse doUpdate(
            @QueryParameter String firstName,
            @QueryParameter String lastName,
            @QueryParameter String email
    ) throws Exception {

        final Attributes attrs = new BasicAttributes();

        attrs.put("givenName", firstName);
        attrs.put("sn", lastName);
        attrs.put("mail", email);

        LdapContext context = factory.connect();
        try {
            context.modifyAttributes(dn,DirContext.REPLACE_ATTRIBUTE,attrs);
        } finally {
            context.close();
        }

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;

        return new HttpRedirect("done");
    }
}
