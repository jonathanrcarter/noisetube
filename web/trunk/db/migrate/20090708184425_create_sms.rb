class CreateSms < ActiveRecord::Migration
  def self.up

    create_table :usersms do |t|
      t.string :phone
      t.timestamps
    end

    add_index :usersms, "phone"

    create_table :sms do |t|
      t.string :text
      t.point :geom, :srid=>4326
      t.string :location
      t.integer :usersms_id
      t.integer :city_id
      t.timestamps
    end

    add_index :sms, "geom", :spatial=>true
  
  end

  def self.down
    drop_table :sms
  end
end
