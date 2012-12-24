package com.qburst.docphin.datamodels;

public class DocphinMyDirectoryModel
{
    private String FullName, Email, CurrentRotation;

    private String[] Phones;

    public String getFullName()
    {
        return FullName;
    }

    public void setFullName(String fullName)
    {
        FullName = fullName;
    }

    public String getEmail()
    {
        return Email;
    }

    public void setEmail(String email)
    {
        Email = email;
    }

    public String[] getPhones()
    {
        return Phones;
    }

    public void setPhones(String[] phones)
    {
        Phones = phones;
    }

    public String getCurrentRotation()
    {
        return CurrentRotation;
    }

    public void setCurrentRotation(String currentRotation)
    {
        CurrentRotation = currentRotation;
    }
}
